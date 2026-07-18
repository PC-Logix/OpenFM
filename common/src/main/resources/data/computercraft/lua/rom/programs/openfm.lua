-- OpenFM radio controller
-- Shipped with OpenFM and available as the `openfm` CraftOS program.

local args = { ... }

local function usage()
    print("Usage: openfm [peripheral]")
    print("Controls an attached OpenFM radio.")
    print("Specify a peripheral name when multiple radios are connected.")
end

if args[1] == "help" or args[1] == "--help" or args[1] == "-h" then
    usage()
    return
end

local function findRadio(requested)
    if requested then
        if not peripheral.isPresent(requested) or not peripheral.hasType(requested, "openfm_radio") then
            return nil, nil, "'" .. requested .. "' is not an OpenFM radio"
        end
        return peripheral.wrap(requested), requested
    end

    local names = peripheral.getNames()
    table.sort(names)
    for _, name in ipairs(names) do
        if peripheral.hasType(name, "openfm_radio") then
            return peripheral.wrap(name), name
        end
    end

    return nil, nil, "No OpenFM radio found. Place one beside the computer or attach it with a wired modem."
end

local radio, radioName, findError = findRadio(args[1])
if not radio then
    printError(findError)
    return
end

local oldBackground = term.getBackgroundColor()
local oldText = term.getTextColor()
local width, height = term.getSize()
local isColor = term.isColor()
local message = "Ready"
local messageIsError = false
local buttons = {}

local theme = {
    background = colors.black,
    panel = isColor and colors.gray or colors.black,
    accent = isColor and colors.lime or colors.white,
    accentDark = isColor and colors.green or colors.black,
    text = colors.white,
    muted = isColor and colors.lightGray or colors.white,
    good = isColor and colors.lime or colors.white,
    bad = isColor and colors.red or colors.white,
    button = isColor and colors.gray or colors.black,
    buttonText = colors.white,
}

local function setColors(foreground, background)
    term.setTextColor(foreground)
    term.setBackgroundColor(background)
end

local function fillLine(y, background)
    if y < 1 or y > height then return end
    setColors(theme.text, background)
    term.setCursorPos(1, y)
    term.write(string.rep(" ", width))
end

local function writeAt(x, y, text, foreground, background)
    if y < 1 or y > height or x > width then return end
    text = tostring(text or "")
    if x < 1 then
        text = text:sub(2 - x)
        x = 1
    end
    text = text:sub(1, math.max(0, width - x + 1))
    setColors(foreground or theme.text, background or theme.background)
    term.setCursorPos(x, y)
    term.write(text)
end

local function fit(text, maxWidth)
    text = tostring(text or "")
    if #text <= maxWidth then return text end
    if maxWidth <= 3 then return text:sub(1, maxWidth) end
    return text:sub(1, maxWidth - 3) .. "..."
end

local function safeGet(method, fallback)
    local ok, value = pcall(method)
    if ok then return value end
    return fallback
end

local function callRadio(label, method, ...)
    local ok, success, reason = pcall(method, ...)
    if not ok then
        message = tostring(success)
        messageIsError = true
        return false
    end
    if success == false then
        message = reason or (label .. " failed")
        messageIsError = true
        return false
    end
    message = label
    messageIsError = false
    return true
end

local function addButton(x, y, label, action, foreground, background)
    if y < 1 or y > height or x > width then return x end
    local shown = " " .. label .. " "
    shown = shown:sub(1, math.max(0, width - x + 1))
    writeAt(x, y, shown, foreground or theme.buttonText, background or theme.button)
    buttons[#buttons + 1] = { x1 = x, x2 = x + #shown - 1, y = y, action = action }
    return x + #shown + 1
end

local function draw()
    width, height = term.getSize()
    buttons = {}
    term.setBackgroundColor(theme.background)
    term.clear()

    fillLine(1, theme.accentDark)
    writeAt(2, 1, "OpenFM Radio Controller", theme.accent, theme.accentDark)

    local playing = safeGet(radio.isPlaying, false)
    local locked = safeGet(radio.getLockState, false)
    local volume = safeGet(radio.getVol, 0)
    local url = safeGet(radio.getURL, "")
    local nowPlaying = safeGet(radio.getNowPlaying, "")
    local screenText = safeGet(radio.getScreenText, "")
    local screenColor = safeGet(radio.getScreenColor, 0xFFFFFF)

    writeAt(2, 3, "Radio", theme.muted)
    writeAt(12, 3, fit(radioName, width - 13), theme.text)
    writeAt(2, 4, "Status", theme.muted)
    writeAt(12, 4, playing and "PLAYING" or "STOPPED", playing and theme.good or theme.bad)
    writeAt(2, 5, "Access", theme.muted)
    writeAt(12, 5, locked and "LOCKED" or "UNLOCKED", locked and theme.bad or theme.good)

    writeAt(2, 7, "Volume", theme.muted)
    local barWidth = math.max(10, math.min(20, width - 18))
    local filled = math.floor((math.max(0, math.min(10, volume)) / 10) * barWidth)
    writeAt(12, 7, "[", theme.muted)
    writeAt(13, 7, string.rep("#", filled), theme.accent)
    writeAt(13 + filled, 7, string.rep("-", barWidth - filled), theme.muted)
    writeAt(13 + barWidth, 7, "] " .. volume .. "/10", theme.muted)

    writeAt(2, 9, "Now playing", theme.muted)
    writeAt(2, 10, fit(nowPlaying == "" and "(waiting for metadata)" or nowPlaying, width - 2), theme.text)
    writeAt(2, 11, "Stream URL", theme.muted)
    writeAt(13, 11, fit(url == "" and "(not set)" or url, width - 14), theme.text)
    writeAt(2, 13, "Display", theme.muted)
    writeAt(12, 13, fit(screenText == "" and "OpenFM" or screenText, width - 20), theme.text)
    writeAt(math.max(2, width - 7), 13, string.format("#%06X", screenColor), theme.accent)

    local controlsY = math.max(14, height - 4)
    local x = 2
    x = addButton(x, controlsY, "P Play", "play", theme.buttonText, playing and theme.accentDark or theme.button)
    x = addButton(x, controlsY, "S Stop", "stop")
    x = addButton(x, controlsY, "- Vol", "down")
    addButton(x, controlsY, "+ Vol", "up")

    x = 2
    x = addButton(x, controlsY + 1, "U URL", "url")
    x = addButton(x, controlsY + 1, "T Text", "text")
    x = addButton(x, controlsY + 1, "C Color", "color")
    x = addButton(x, controlsY + 1, "R Refresh", "refresh")
    addButton(x, controlsY + 1, "Q Quit", "quit")

    fillLine(height, theme.panel)
    writeAt(2, height, fit(message, width - 2), messageIsError and theme.bad or theme.muted, theme.panel)
    term.setCursorBlink(false)
end

local function prompt(label, initial)
    fillLine(height, theme.panel)
    writeAt(2, height, label .. ": ", theme.accent, theme.panel)
    term.setCursorBlink(true)
    local value = read(nil, nil, nil, initial or "")
    term.setCursorBlink(false)
    return value
end

local function perform(action)
    if action == "play" then
        callRadio("Playback started", radio.start)
    elseif action == "stop" then
        callRadio("Playback stopped", radio.stop)
    elseif action == "up" then
        callRadio("Volume raised", radio.volUp)
    elseif action == "down" then
        callRadio("Volume lowered", radio.volDown)
    elseif action == "url" then
        local value = prompt("Stream URL", safeGet(radio.getURL, ""))
        callRadio(value == "" and "Stream URL cleared" or "Stream URL updated", radio.setURL, value)
    elseif action == "text" then
        local value = prompt("Display text", safeGet(radio.getScreenText, ""))
        callRadio("Display text updated", radio.setScreenText, value)
    elseif action == "color" then
        local current = safeGet(radio.getScreenColor, 0xFFFFFF)
        local value = prompt("Display color (RRGGBB)", string.format("%06X", current))
        value = value:gsub("^#", ""):gsub("^0[xX]", "")
        local rgb = #value == 6 and tonumber(value, 16) or nil
        if rgb then
            callRadio("Display color updated", radio.setScreenColor, rgb)
        else
            message = "Color must be exactly six hexadecimal digits"
            messageIsError = true
        end
    elseif action == "refresh" then
        message = "Refreshed"
        messageIsError = false
    elseif action == "quit" then
        return false
    end
    return true
end

local function run()
    local running = true
    while running do
        draw()
        local event = { os.pullEventRaw() }
        local name = event[1]

        if name == "terminate" then
            running = false
        elseif name == "term_resize" then
            -- Redrawn on the next loop.
        elseif name == "char" then
            local key = event[2]:lower()
            local actions = {
                p = "play", s = "stop", u = "url", t = "text",
                c = "color", r = "refresh", q = "quit",
                ["+"] = "up", ["="] = "up", ["-"] = "down",
            }
            if actions[key] then running = perform(actions[key]) end
        elseif name == "mouse_click" then
            local x, y = event[3], event[4]
            for _, button in ipairs(buttons) do
                if y == button.y and x >= button.x1 and x <= button.x2 then
                    running = perform(button.action)
                    break
                end
            end
        elseif name == "peripheral_detach" and event[2] == radioName then
            message = "Radio disconnected"
            messageIsError = true
        elseif name == "peripheral" or name:sub(1, 7) == "openfm_" then
            message = name:gsub("openfm_", "Radio "):gsub("_", " ")
            messageIsError = false
        end
    end
end

local ok, err = pcall(run)
term.setCursorBlink(false)
term.setBackgroundColor(oldBackground)
term.setTextColor(oldText)
term.clear()
term.setCursorPos(1, 1)

if not ok then error(err, 0) end
