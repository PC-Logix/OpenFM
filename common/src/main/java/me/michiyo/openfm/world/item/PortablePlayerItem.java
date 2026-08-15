package me.michiyo.openfm.world.item;

import me.michiyo.openfm.net.PortablePlayerSync;
import me.michiyo.openfm.reg.ModItems;
import me.michiyo.openfm.world.menu.PortablePlayerMenu;
import me.michiyo.openfm.platform.Services;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

public class PortablePlayerItem extends Item {
    private static final String ROOT = "OpenFMPortable";
    private static final String URL = "StreamUrl";
    private static final String VOLUME = "Volume";
    private static final String PLAYING = "Playing";
    private static final String PRIVATE = "Private";
    private static final String CARD = "MemoryCard";
    private static final String CARD_DATA = "OpenFMCard";

    public PortablePlayerItem(Properties props) {
        super(props.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            if (player.isShiftKeyDown()) {
                if (togglePlayback(stack)) PortablePlayerSync.sync(serverPlayer);
            } else {
                Services.PLATFORM.openStandardMenu(serverPlayer,
                        new net.minecraft.world.SimpleMenuProvider(
                                (id, inventory, menuPlayer) -> new PortablePlayerMenu(id, inventory),
                                Component.literal("Portable Streamer")));
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    public static ItemStack findHeldStack(Player player) {
        if (player.getMainHandItem().is(ModItems.PORTABLE_PLAYER.get())) return player.getMainHandItem();
        if (player.getOffhandItem().is(ModItems.PORTABLE_PLAYER.get())) return player.getOffhandItem();
        return ItemStack.EMPTY;
    }

    public static String getUrl(ItemStack stack) {
        return data(stack).getString(URL).trim();
    }

    public static int getVolume(ItemStack stack) {
        return Math.max(0, Math.min(10, data(stack).getInt(VOLUME)));
    }

    public static boolean isPlaying(ItemStack stack) {
        return data(stack).getBoolean(PLAYING);
    }

    public static boolean isPrivate(ItemStack stack) {
        return data(stack).getBoolean(PRIVATE);
    }

    public static void setUrl(ItemStack stack, String url) {
        update(stack, root -> root.putString(URL, url == null ? "" : url.trim()));
    }

    public static void setVolume(ItemStack stack, int volume) {
        update(stack, root -> root.putInt(VOLUME, Math.max(0, Math.min(10, volume))));
    }

    public static void setPlaying(ItemStack stack, boolean playing) {
        update(stack, root -> root.putBoolean(PLAYING, playing));
    }

    public static boolean togglePlayback(ItemStack portable) {
        boolean canPlay = !getUrl(portable).isEmpty() && getVolume(portable) > 0;
        if (!canPlay && !isPlaying(portable)) return false;
        setPlaying(portable, !isPlaying(portable));
        return true;
    }

    public static void setPrivate(ItemStack stack, boolean privateOnly) {
        update(stack, root -> root.putBoolean(PRIVATE, privateOnly));
    }

    public static void stopPlayback(Player player) {
        for (ItemStack stack : player.getInventory().items) stopPlayback(stack);
        for (ItemStack stack : player.getInventory().offhand) stopPlayback(stack);
    }

    public static void stopPlayback(ItemStack stack) {
        if (!stack.is(ModItems.PORTABLE_PLAYER.get())) return;
        setPlaying(stack, false);
        setPrivate(stack, false);
    }

    public static ItemStack getEmbeddedCard(ItemStack portable) {
        CompoundTag root = data(portable);
        if (!root.contains(CARD)) return ItemStack.EMPTY;

        ItemStack card = new ItemStack(ModItems.MEMORY_CARD.get());
        CompoundTag saved = root.getCompound(CARD);
        CustomData.update(DataComponents.CUSTOM_DATA, card, tag -> tag.merge(saved));
        return card;
    }

    public static void saveEmbeddedCard(ItemStack portable, ItemStack card) {
        if (card.isEmpty()) {
            update(portable, root -> root.remove(CARD));
            return;
        }

        CompoundTag cardData = card.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        update(portable, root -> {
            root.put(CARD, cardData);
        });
    }

    public static CompoundTag getSavedCardData(ItemStack card) {
        CompoundTag tag = card.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.contains(CARD_DATA) ? tag.getCompound(CARD_DATA).copy() : new CompoundTag();
    }

    public static void saveUrlToCard(ItemStack card, String url, int volume) {
        CompoundTag root = new CompoundTag();
        root.putString(URL, url == null ? "" : url.trim());
        root.putInt(VOLUME, Math.max(0, Math.min(10, volume)));
        CustomData.update(DataComponents.CUSTOM_DATA, card, tag -> tag.put(CARD_DATA, root));
    }

    public static void loadFromCard(ItemStack portable, ItemStack card) {
        CompoundTag root = getSavedCardData(card);
        if (!root.isEmpty()) {
            setUrl(portable, root.getString(URL));
            if (root.contains(VOLUME)) setVolume(portable, root.getInt(VOLUME));
        }
    }

    public static void togglePrivate(ServerPlayer player) {
        ItemStack headphones = player.getMainHandItem();
        ItemStack portable = player.getOffhandItem();
        if (!headphones.is(ModItems.HEADPHONES.get()) || !portable.is(ModItems.PORTABLE_PLAYER.get())) return;

        boolean next = !isPrivate(portable);
        setPrivate(portable, next);
        player.displayClientMessage(Component.literal(next
                ? "Headphones on: only you hear the portable streamer."
                : "Headphones off: the portable streamer is public.").withStyle(ChatFormatting.GRAY), true);
        PortablePlayerSync.sync(player);
    }

    private static CompoundTag data(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.contains(ROOT) ? tag.getCompound(ROOT) : new CompoundTag();
    }

    private interface RootUpdate { void apply(CompoundTag root); }

    private static void update(ItemStack stack, RootUpdate update) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            CompoundTag root = tag.contains(ROOT) ? tag.getCompound(ROOT) : new CompoundTag();
            update.apply(root);
            tag.put(ROOT, root);
        });
    }
}
