# Jarvis AI — Android AI Assistant (Gemini)

ফুল মোবাইল কন্ট্রোল করা যায় এমন রিয়েল-টাইম ভয়েস অ্যাসিস্ট্যান্ট। বাংলা, ইংলিশ, হিন্দি সহ যেকোনো ভাষায় মানুষের মতো কথা বলে।

## ইনস্টল
1. `JarvisAI-debug.apk` ফোনে ইনস্টল করুন (Unknown sources allow করতে হবে)।
2. অথবা Android Studio-তে এই ফোল্ডার খুলে Run দিন। GitHub-এ push করলে `.github/workflows/build.yml` নিজে থেকেই APK বানিয়ে দেবে।

## প্রথমবার সেটআপ (একবারই করতে হবে)
1. অ্যাপ খুলুন, তারপর যতগুলো পারমিশন চাইবে সব **Allow** করুন।
2. **SETTINGS → Gemini API Key**-তে কী পেস্ট করুন। ফ্রি কী পাবেন: https://aistudio.google.com/apikey। তারপর **Test key** আর **SAVE** চাপুন।
3. Settings-এর **Permissions** সেকশনে যেগুলোতে ○ আছে, প্রতিটায় চাপ দিয়ে চালু করুন:
   - **Accessibility** → Jarvis AI ON (স্ক্রিন দেখা, ট্যাপ, স্ক্রল, টাইপ, সব অ্যাপ কন্ট্রোল)
   - **Notification access** → WhatsApp/Messenger মেসেজ পড়া ও রিয়েল-টাইম রিপ্লাই
   - **Display over other apps** → ভাসমান Jarvis orb
   - **Default Digital Assistant** → Digital assistant app = Jarvis AI (Home লং-প্রেস করলে Google-এর বদলে Jarvis আসবে)
   - **Ignore battery optimization** → "Hey Jarvis" সবসময় চালু থাকবে
   - **Modify system settings** আর **All files access**
4. Xiaomi/Oppo/Vivo/Realme ফোনে: App info → **Autostart ON**, আর Battery → **No restrictions** করে দিন। না হলে ফোন অ্যাপটা বন্ধ করে দিতে পারে।

## ব্যবহার
- **"Hey Jarvis" / "জারভিস" / "হে সার্ভিস" / "মাইরা"** বললেই অ্যাক্টিভ হয়ে যাবে (Settings থেকে wake word বদলাতে পারবেন)।
- মাঝখানের orb-এ ট্যাপ করলে **Live voice** চালু হয়। মাইক একবার চালু হলে চালুই থাকে, বারবার চাপতে হবে না। কথার মাঝে থামিয়েও কথা বলতে পারবেন।
- নিচে টাইপ করে চ্যাটও করা যায়। চ্যাট মোড `gemini-4-argon` দিয়ে চলে, আর ধাপে ধাপে ফোনের কাজ করে দেয়।
- **SCREEN** বাটন চাপলে পুরো স্ক্রিন মিরর হয়, তখন Jarvis লাইভ স্ক্রিন দেখতে পারে।
- **HISTORY**-তে সব কথা (চ্যাট, ভয়েস, কল, অটো-রিপ্লাই) সেভ থাকে, সার্চও করা যায়। Jarvis দরকারি তথ্য মনে রাখে (memory)।

### উদাহরণ কমান্ড
- "YouTube খোলো, অরিজিৎ সিং-এর গান চালাও"
- "রাহুলকে WhatsApp-এ লেখো আমি ১০ মিনিটে আসছি"
- "মাকে ফোন করো স্পিকারে"
- "Facebook খুলে স্ক্রল করো" / "Instagram reels স্ক্রল করো"
- "গ্যালারির শেষ ভিডিওটা YouTube-এ আপলোড করো, টাইটেল দাও My Vlog"
- "সকাল ৬টায় অ্যালার্ম দাও"
- "আমার স্ক্রিনে কী আছে বলো"
- "একটা ক্যালকুলেটর ওয়েবসাইট বানাও আর প্রিভিউ দেখাও" (কোড Downloads/JarvisAI-তে সেভ হবে)
- "Python-এ একটা টু-ডু অ্যাপ লেখো আর ফাইল সেভ করো"
- "PUBG খোলো", "ChatGPT খোলো", "Free Fire খোলো" (ফোনে ইনস্টল থাকা যেকোনো অ্যাপ খুলবে)
- "স্ক্রিন লক করো", "ফোন রিস্টার্ট করো", "ব্রাইটনেস ৫০%", "টর্চ জ্বালাও"

## মডেল (Settings থেকে বদলানো যায়)
| কাজ | ডিফল্ট মডেল |
|---|---|
| চ্যাট + ফোন কন্ট্রোল | `gemini-4-argon` (অপশন: `gemini-3.8-flash`, `gemini-flash-latest`) |
| রিয়েল-টাইম ভয়েস | `gemini-3.8-live` (native audio) |
| অটো-রিপ্লাই | `gemini-3.8-flash` |

## কল ফিচার
- কল এলে কে ফোন করছে তার নাম বলে দেয়।
- **Auto-answer** চালু করলে Jarvis নিজে কল ধরবে, স্পিকার অন করবে, কলারের সাথে কথা বলবে, মেসেজ নিয়ে রাখবে, তারপর কল কেটে দেবে।
- ভয়েসে বলতে পারেন: "কলটা ধরো", "কল কেটে দাও"।

## Android-এর সীমাবদ্ধতা (সৎভাবে জানিয়ে রাখছি)
- **ফোন বন্ধ (power off) থাকলে** কোনো অ্যাপই চলতে পারে না। "অন করা" মানে এখানে স্ক্রিন জাগানো। Power off/Restart-এর জন্য Jarvis পাওয়ার মেনু খুলে বাটনে চাপ দেয়।
- **কলের অডিও**: Android কোনো সাধারণ অ্যাপকে সরাসরি কলের আওয়াজ শুনতে দেয় না। তাই Jarvis স্পিকার অন করে মাইক দিয়ে শোনে আর স্পিকারে কথা বলে।
- **Wake word**: স্ক্রিন অফ থাকলে কিছু ফোন মাইক বন্ধ করে দেয়। Battery optimization off আর Autostart on রাখলে সবচেয়ে ভালো কাজ করে।
- আপলোড/পোস্টিং অ্যাপের স্ক্রিন দেখে ধাপে ধাপে ট্যাপ করে হয়। কোনো অ্যাপ নিজের ডিজাইন বদলালে Jarvis আবার স্ক্রিন পড়ে নতুন পথ খুঁজে নেয়।
- Google Play-তে এত পারমিশনওয়ালা অ্যাপ পাবলিশ করা কঠিন, তবে নিজের ফোনে ব্যবহারে কোনো সমস্যা নেই।

## প্রজেক্ট স্ট্রাকচার
```
app/src/main/java/com/jarvis/ai/
  core/   Prefs (settings), Memory (history+memory), Persona (system prompt),
          GeminiChat (REST agent loop), LiveSession (real-time voice WebSocket)
  tools/  ToolDefs (40+ functions), ToolExecutor (implementation), AppFinder (all apps)
  service/ AssistantService (wake word + live voice + floating orb),
           JarvisAccessibilityService (screen + control), ScreenCapture (mirroring),
           JarvisNotificationListener (auto reply), Receivers (boot, calls),
           VoiceInteraction (digital assistant role)
  ui/     MainActivity (HUD UI), OrbView, SettingsActivity, Permissions, History/WebPreview/ScreenShare
```

## v1.4.0 — Install by Touch, Family Wake Words, Accessibility Watchdog, New Repo + ZIP Push, Calendar, Health, Supabase, Password Vault, Wellness, Meme Sort, Focus Watch (2026-11-15)

- **App install — screen touch kore:** **\"WhatsApp install koro\"** bollei Jarvis Play Store page khule nije \"Install\"-e tap korbe, install hoyeche kina check kore janabe. APK file holeo tai (\"install my-app.apk\"). Silent install-er kono API Google dey na — screen tap-i sotyi upay. Accessibility ON lagbe.
- **Family wake words:** prottek family member-er nijer wake word — **\"ammu dakche\" → Ammu, \"baba suno\" → Baba**. Je dakbe, Jarvis take sei naam-e dakbe. Settings-eo add/remove kora jay, **add_wake_word / list_wake_words / remove_wake_word** tool-eo.
- **Accessibility watchdog:** vivo/Xiaomi/Oppo update ba battery kill-er por Accessibility bondho hoye gele Jarvis 15 minute-e check kore notification debe — tap korlei settings khulbe. Home screen-eo warning dekhabe. Ekbar App info → ⋮ → **\"Allow restricted settings\"** ON korle ar bondho hobe na (Settings > Permissions-e button ache). Kono app nije theke Accessibility ON korte pare na — Android-e eta nishiddho.
- **Notun GitHub repo + ZIP push:** **\"create repo my-website\"** — notun repo banabe (public/private), setai active repo hobe. **\"push zip my-site.zip\"** — ZIP unzip kore sob text file GitHub-e push korbe (binary / 1MB-r boro file skip), phone-eo workspace/unzipped/-e extract kore rakhbe.
- **Smart calendar sync:** **\"aaj amar ki appointment ache?\"** — phone-er calendar (Google Calendar sync hoy) pore janabe. **\"add event\"** — appointment add korbe, clash thakle age thekei sotorko korbe.
- **Health tracking:** **health_status** — phone-er step counter theke aaj-er steps, distance, calories (anuman); heart-rate sensor thakle reading. BODY_SENSORS + ACTIVITY_RECOGNITION permission add holo, Health Connect permission screen kholar button-o ache (Settings > Health tracking).
- **SMS:** age thekei ache — **send_sms** tool, SEND/READ/RECEIVE_SMS permission declared, app khullei chabe. Pathanor age Jarvis recipient + message confirm korbe.
- **WhatsApp connect:** notification access diye read/reply age thekei ache; **WhatsApp Web** browser-e khule QR scan kore link kora jay — WhatsApp-er kono official API nei, tai ei dutoi sotyi upay. **whatsapp_web** tool browser-e web.whatsapp.com khule debe.
- **Supabase (optional cloud):** Settings > Supabase-e Project URL + anon key bosale memory/history/settings cloud-e sync hobe (**supabase_sync up/down**, table `jarvis_sync`). Na korleo Jarvis local-e 100% cholbe — Supabase-er opor kichu depend kore na. Realtime mane 15 min-e ekbar check + on-demand, live websocket noy.
- **Object recognition:** **identify_object** — screen-er screenshot dekhe Gemini Vision bolbe ki ki object ache, kothay ache. Eta camera + AI vision — sotyi AR (ARCore 3D overlay) noy. Gemini API key lagbe.
- **Predictive notification:** Jarvis tomar obhyash dekhe (kon ghontay ki koro) ghontay 1 bar-er beshi na, mone koriye debe — Settings > Features-e off kora jay.
- **Mood recommendation:** **recommend** — mood onujayi gaan/video/activity suggest korbe (mon kharap → comedy + happy gaan, klanto → lofi + nap, tension → meditation).
- **Phone finder:** **\\"phone kothay?\\"** — **find_phone** jore alarm bajabe + vibrate korbe + torch jalabe/nivabe (silent mode-eo alarm baje); **\\"stop\\"/stop_ringing** bolle thambe.
- **Daily briefing:** **daily_briefing** — weather (Open-Meteo, free, kono key lagbe na), aaj-er calendar, reminders, top news — ekta choto summary; workspace/daily-briefing-date.md-teo save hoy. Routine-e action **\\"briefing\\"** dile prottek din sokal 7tay asbe.
- **Offline translation:** **translate_offline** — internet charai Bengali ↔ English dictionary (~150 word + phrase) diye word-by-word; .txt/.md file-o translate kore. Puro sentence-er jonno online **translate** (Gemini) use koro.
- **Password vault:** **save_password / get_password / list_passwords / delete_password** — password AES-256-GCM (Android Keystore) diye encrypted vault-e thake. List-e sudhu site-er naam dekha jay — password kokhono jore bola, memory-te lekha ba share kora hoy na; get_password sudhu login form-e type korar jonno.
- **Wellness tracker:** **log_sleep** (\\"11:30pm\\" → \\"7:00am\\") — ghum-er duration + 7-din-er average (goal 8h); **log_water** (\\"1 glass\\" = 250ml) — dine 2000ml goal; **wellness_status**-e summary. Manual log — watch ba onno health app theke ghum pora jay na.
- **Precise clicks:** tap ekhon exact-match age khuje (\\"Install\\" kokhono \\"Uninstall\\"-e tap korbe na), tap-er por screen bodlalo kina check kore, na bodlale porer button try kore, ar ek-i button-e 400ms-er moddhe dubar tap kore na.
- **Fast wake:** \\"Hey Jarvis\\" bollei sathe sathe vibrate + overlay dekhabe — wake detection ~0.3–0.8 second; local command-er uttor millisecond-e. 1 millisecond sombhob na (mic + audio buffer + recognition-e somoy lagei).
- **Meme auto-sort:** **sort_memes** — folder-er meme gulo type (images/gifs/videos) + month-wise (images/2026-11/) sort korbe, duplicate **Duplicates/** folder-e sorabe — kokhono delete kore na. Default workspace/memes; `downloads` / `pictures`-o chole.
- **Kaj faki dhora (focus report):** **focus_report** — kaj-er somoye (10:00–18:00) kon app kotokhon khola chilo, YouTube/Instagram/game naam dhore bolbe + verdict debe. Usage Access permission lagbe (Jarvis settings khule debe); sudhu EI phone-er hiseb — onno karo phone dekhte pare na.
- **Auto online/offline switch:** internet chole gele Jarvis nije thekei offline/local mode-e chole jay, ar internet fire ele nije thekei online hoye jay — prottekbar notification diye janabe. Wi-Fi connect thakleo asol internet na thakle (captive portal / router down) Jarvis offline hisebe kaj korbe. **network_status** diye ekhon-er obostha dekha jay.
- **Control Panel access:** **open_control_panel** — phone-er Control Center (Quick Settings) khule debe; **toggle_setting** — Wi-Fi, Bluetooth, Aeroplane mode, Location, Mobile data, Hotspot, Dark mode, Eye Protection, Battery Saver — panel-er tile-e tap kore on/off korbe (Accessibility ON lagbe). Torch direct, Do Not Disturb direct (Notification access lagbe), Brightness / Auto-rotate (Modify system settings ekbar lagbe), Volume direct. Android kono app-ke chupchap Wi-Fi/Airplane/Location toggle korte dey na — panel tap-i sotyi upay.
- **Liquid-glass UI + button animations + Light/Dark mode:** button-e tap korle halka choto hoye bounce-back kore (press animation), notun chat bubble niche theke fade-in hoy, chip/card/input-e liquid-glass look (adha-sotoccho fill + halka border + highlight). Settings > Personalize theke **Light mode** on korle puro main screen + Settings halka rong-er hoye jay (dark default).
- **Wallpaper changer:** **set_wallpaper** — background-e, kichu na khule, phone-er wallpaper bodle debe — home screen, lock screen, ba duita-i. Source: "random" (internet theke sundor photo), kono image URL, ba workspace-er file (workspace/ ba workspace/wallpapers/).
- **Automatic messaging system:** **schedule_message** — nirdishto somoye SMS nije theke chole jabe, ekbar ba prottek din (jemon prottek sokal 8tay Ammu-ke "Good morning"). Recipient + message age confirm kore nio. WhatsApp-e scheduled message sombhob noy (official API nei) — SMS-i automatic route. Routine-eo action "message:<to>:<text>" diye message pathano jay.
- **Wake screen:** **wake_screen** — screen jaliye debe (lock screen dekhabe). Sotyi kotha: kono app PIN/password diye lock screen khulte pare na — shudhu tumi nije, ba Android-er Extend Unlock (Smart Lock) trusted jaygay/device-e phone unlocked rakhe (Settings > Security > Extend Unlock, ekbar setup).
- **Owner backend (Render):** **backend_status** — prottek 15 min-e Jarvis backend-e heartbeat pathay (install ID, device model, app version, last action). Owner dashboard-e (jarvis-backend/) dekhte parbe ke install koreche, ke active, ke ki korche — ar jekono user-ke **ban** (sarajibon), **suspend** (7 din), **shutdown** (30 din ba sarajibon) ba **restore** korte parbe. Backend URL Settings > Backend theke dite hoy; URL na thakle ba backend down thakle Jarvis local-e 100% cholbe — backend kokhono normal kaj block kore na.
- **Fingerprint/biometric lock puropuri tule deoa holo (crash fix):** vivo-te BiometricPrompt khulle app bondho hoye jachhilo, tai BioLock class, USE_BIOMETRIC permission, ar Settings-er biometric switch — sob sorano holo. Ekhon app khulte kono fingerprint prompt asbe na; sudhu optional passphrase lock ache (OFF by default).
- **Full anime animation:** main screen-e slow aurora background (anime-style rong bodlay — dark-e navy/purple/teal, light-e sky/mint/lavender), orb-ta breathing pulse kore (halka boro-choto), ar screen bodle gele fade transition hoy. Background animation onPause-e theme jay (battery bachate).
- **Backend automatic + hidden:** live backend URL (`https://jarvis-backend-8ray.onrender.com`) app-er bhetore default bosano — install korlei heartbeat automatic jabe, Settings-e kichu bosate hobe na; Backend section-ta Settings theke hide kora.

## v1.4.1 — Owner Admin System v2 (ek jaygay, simple)
- **One-click BAN user + BAN device:** dashboard-e boro BAN USER button; BAN DEVICE korle oi phone-er sob install bondho (device_id = Android ID-er SHA-256 hash). Suspend 7d / Shutdown 30d / Restore-o ache.
- **Backend theke feature add/on-off:** dashboard theke kono tool OFF korle app-e seta cholbe na; extra instruction (persona_extra) sob install-er nirdesh-e jog hoy; announcement app-er upore banner-e dekhay.
- **Online update:** dashboard-e notun version + APK URL dile sob install download kore "tap to install" notification dey — bar bar APK khujte/download korte hoy na. SOTYI KOTHA: puropuri silent update Android dey na — ekbar Install tap kortei hobe (Play Store chara keu pare na).
- **Ki korche dekha (sudhu monitoring ON thakle):** recent activity, kon permission diyeche/denay, sesh chat history, request korle ekta screenshot / ekta camera photo (live video na), Jarvis workspace-er choto text file (din-e ekbar backup).
- **Owner monitoring switch (Settings, OFF by default):** user nije ON korle tobei uporer data jay; OFF thakle owner sudhu device/version/last action dekhte pay. Password/OTP/location/onno app-er data — kokhono jay na.
- **About & Policy (Settings):** soja soja Banglish-e lekha — admin ki use kore, ki kore na, APK ki use kore, ki kore na.
- Backend v2: `/api/config`, `/api/history`, `/api/screenshot`, `/api/camera-photo`, `/api/files`, device ban, remote config, notun dashboard (user card, permissions chips, history, screen, camera, files, Policy panel).

## v1.4.2 — Owner Face Check + WhatsApp call answer
- **Owner Face Check (Settings > Owner Face Check, OFF by default):** owner age nijer photo save kore (front-camera selfie ba gallery theke). Jarvis kholar somoy front camera selfie tule Gemini Vision diye owner-er photo-r sathe miliye dekhe — mille khulbe, na mille Jarvis bondho thakbe (chat + live voice dujonei). 5 min-er beshi background-e thakle abar check hoy. Tool: `verify_owner` (check koro), `save_owner_photo` (selfie tule owner photo save koro). SOTYI KOTHA: eta sudhu JARVIS APP-er check — phone-er lock na, onno app bondho hobe na, phone-er PIN/fingerprint-er bodoleo na. Face compare Gemini Vision-e hoy (internet + Gemini key lagbe); key/net/camera na thakle check cholbe na — tokhon Jarvis khula thakbe ar karon-ta janabe. Alo kharap / chehara bodle gele notun photo dao.
- **WhatsApp call answer:** `answer_whatsapp_call` — WhatsApp-er incoming-call screen-er Answer button-e tap kore, speaker ON kore, caller-ke greeting bole ("Hello, ami Jarvis. Ektu wait korun, ami owner ke dakchi."). Settings > Features-e "Auto-answer WhatsApp calls" switch — ON thakle call aslei nije theke dhore. SOTYI KOTHA: Jarvis call-er asol participant hote pare na — Android kono app-ke WhatsApp-er mic-e audio dhokate dey na. Greeting-er por owner-ke phone nite hobe. Phone lock thakle swipe lagte pare — tokhon nije uthate hobe.

## v1.4.3 — Away Mode (WhatsApp answering machine)
- **Away Mode (Settings > Features, OFF by default):** ON thakle Jarvis WhatsApp call dhore away message bole ("Owner ekhon barite nei, pore call koro. Dhonnobad!" — Settings-e nijer kotha likhte paro) ar tarpor nijei call kete dey — answering machine-er moto. Barite na thakleo kaj kore (phone on + net + Accessibility + auto-answer ON thaklei hobe). Voice-eo control kora jay: `set_away_mode` ("away mode on koro" / "away mode off koro"). OFF thakle sudhu greeting bole owner-er jonno wait kore.

## v1.4.4 — Heartbeat fix (dashboard-e real device asbe)
- **Bug chilo:** purono version-er Settings SAVE khali field theke `backend_url=""` save kore felechilo — tai app kokhono heartbeat pathato na, dashboard-e kono device ashto na (backend thik chilo, app thekei jeto na). Ekhon khali URL thakle live backend URL-ta nije thekei use hobe ar save-o hoye jabe.
- **App khullei heartbeat:** ar 15 min wait korte hobe na — Jarvis kholar sathe sathe device dashboard-e dekhabe (tarpor 15 min-e ekbar kore update hobe).

**Ja kora jay na (sotyi kotha):** onno app-er sathe health data integration (sleep/weight/BP watch theke — ei version-e pora jay na); RTMP live streaming (YouTube/Twitch-e live); system-wide autofill (sob site-e password bhora); app cloning (onno app-er duplicate banano — vivo-r built-in App Clone use koro); silent Play Store install; Accessibility nije theke ON kora; 1 millisecond-e wake (mic + recognition-e somoy lagei — ~0.3–0.8s-e hoy); onno karo phone-er usage dekha.

## v1.3.9 — Mega Update: Gestures, Mood, Offline, Security, Sync, Dashboard, Automation (2026-11-15)

- **One-click Facebook connect:** Settings > Connect > **Connect Facebook (one click)** — kono API, kono key lagbe na, login page khule email/password dilei connect. **\"Facebook connect koro\"** bolleo hobe.
- **Gesture control:** phone **shake korle Jarvis jagbe** (Settings > Gesture Control), orb-e **swipe UP = kotha shuru, swipe DOWN = thambe**. **\"Shake korle YouTube kholo\"** bolle gesture-ta shikhe nebe (set_gesture) — tarpor theke shake korlei YouTube khulbe.
- **Real-time mood detection:** Jarvis tomar kotha theke mood bujhbe (khushi / mon kharap / rag / klanto / tension / bored / confused) ar sei moto uttor debe — mon kharap thakle norom kore, khushi thakle moja kore. Humour-o mood onujayi bodle jay.
- **Offline mode:** internet na thakle Jarvis nijei offline hoye jay — time, battery, torch, lock, app khola, call, alarm, scroll/back sob offline-e cholbe. Settings-e force-o kora jay.
- **Advanced battery:** **\"battery status\"** bolle full report — level, charging, health, temperature, voltage, charge, current.
- **Multilingual real-time translation:** **\"translate koro\"** — Bengali, English, Hindi, Spanish, Arabic, French — jekono bhasha.
- **Adaptive learning + predictive AI:** Jarvis tomar habit shikhe — kon somoy ki koro — ar age thekei suggest kore (✨ suggestion chips). **\"predict_next\"** bolle dekhabe tumi erpor ki chaibe.
- **Smart scheduling + automation:** **\"remind me at 6pm to call mom\"** — reminder debe. **create_routine** — \"prottek din sokal 7tay good morning bolo\" ba \"charger lagale light on koro\" — nije nije korbe.
- **Advanced security suite:** **security_scan** — screen lock, encryption, unknown sources, kon app-er kache SMS/mic/camera/location permission ache. **diagnostics** — battery, storage, memory, CPU, Android version, security patch — root charai. **troubleshoot** — problem khuje nije fix korbe (cache clean, history trim, low battery-te disco off). **network_audit** — TOMAR Wi-Fi-er security check (open Wi-Fi? VPN?) — onner network kokhono scan kore na.
- **Encrypted local storage:** memory + history AES-256-GCM diye encrypted thake (key Android Keystore-e). Keu file copy korleo porte parbe na.
- **Self-healing:** 3 bar crash korle Jarvis nije safe mode-e jay (hotword/disco/shake off) ar tomake janay. **view_logs** diye nijer log dekhte pare.
- **Sync + cloud backup:** memory + settings tomar NIJER GitHub repo diye onno device-e sync hoy (private repo use koro; API key kokhono sync hoy na). **backup_preferences / restore_preferences** — shudhu settings-er cloud backup. **session_handoff** — ei session-ta onno device-e continue korte parbe.
- **Direct memory export:** **export_memory** — puro memory ekta JSON file-e Downloads > JarvisAI-te (Settings > Memory & history thekeo).
- **Personalized UI:** theme rong bodlao — Cyan, Orange, Pink, Green, Purple (**\"theme green koro\"**). Orb-er rong sathe sathe bodle jay.
- **App lock (optional, OFF by default):** passphrase ba fingerprint/face diye Jarvis lock kora jay — Settings > App Lock. v1.3.6-e lock tule deoa hoyechilo karon problem korchilo, tai eta sudhu tumi chaile on koro. Eta sudhu JARVIS app-ke lock kore, phone-er lock screen-ke noy.
- **Dashboard:** header-e **DASHBOARD** button — battery, memory, storage, CPU, network, mood, top tools, predictions, schedules, security — 5 second-e refresh hoy.
- **Browser upgrade:** back / forward / reload button, **★ bookmark** (save kore rakho, tap korlei khulbe), file download Downloads-e jay.
- **Self-correcting deployment:** **fix_build** — GitHub Actions build fail korle log pore karon ber korbe, jeta fix kora jay nije fix kore abar build chalabe. **build_bundle** — Play Store-er jonno .aab (App Bundle) banabe. **check_update** — notun build ele download korbe. **check_models** — notun Gemini model ele janabe.
- **API connectivity:** **api_call** — jekono REST API call korte pare (Zapier, IFTTT, Notion, Telegram, tomar server).
- **Task delegation:** **delegate_task** — ekta kaj background worker-ke diye dao (research, draft) — sesh hole notification debe, result workspace/delegated/-e thakbe.
- **Keep alive:** **keep_alive** — battery optimization exemption, jate Android Jarvis-ke background-e na mare; hotword listener error hole nijei restart hoy.
- **Voice accessibility commands:** **\"scroll down\", \"go back\", \"home\", \"open notifications\", \"tap <text>\"** — kono AI round-trip charai, offline-eo cholbe.
- **Integrity chain:** **verify_integrity** — Jarvis-er data bodle geche kina hash chain diye check kore (local ledger, public blockchain noy).
- **Bug fix:** v1.3.8-e `device_status` duibar chilo bole smart home-er device list kokhono asto na — ekhon thik ache (`device_status` = smart home devices, `phone_status` = phone-er status).

**Sotyi kotha (ja kora jay na):** phone root kora jay na (warranty jay, brick hote pare); zero latency / 1ms impossible (local command millisecond-e hoy, kintu network + AI-e somoy lage); phone-er PIN/password/fingerprint lock kono app khulte pare na; Google-er model retrain kora jay na, sudhu notun model-e switch kora jay; onner network/device scan ba attack kora illegal — Jarvis seta kore na.

## v1.3.8 — Smart Home + ElevenLabs Voice + Self-Feature (2026-11-15)

- **Smart home control:** Home Assistant thakle Jarvis light/fan/AC/switch on-off korte pare (**control_device**, **device_status**). URL + token Settings > Smart Home-e bosao. Na thakle Google Home app khule debe.
- **ElevenLabs voice:** Settings > ElevenLabs Voice-e API key + Voice ID bosale Jarvis oi voice-e kotha bolbe (eleven_multilingual_v2). Na thakle phone-er voice.
- **Nije nije feature add kora (sotyi vabe):** **request_feature** bolle Jarvis request-ta save kore GitHub-e push korbe — tarpor code likhe, **build_apk** diye notun version baniye, tumi install korlei feature chole asbe. Cholonto code nije bodlate pare na — keu ei kotha bolle seta mitthe.

## v1.3.7 — Website Login / Accounts / Sensitive Work

- **Jekono website-e login korte pare:** **"Facebook-e login koro"**, **"Instagram-e account kholo"**, **"GitHub-e login koro"** — bollei login/signup page khulbe, Jarvis form-e email/password type kore login korbe. Google, Facebook, Instagram, GitHub, Amazon, Flipkart, X, Netflix, LinkedIn, Render, Netlify — sob site-er login page chene.
- **Account create korte pare:** signup page khule form bhorbe — naam/email/password tumi dilei.
- **Sensitive kaj korte pare — kintu confirm niye:** taka, payment, account delete, password change, message pathano — egulo korar age Jarvis prothome tomake jiggesh korbe. Chupchap kokhono sensitive kaj korbe na.
- **OTP / 2FA:** site OTP chaile code-ta dao — Jarvis type kore debe. Captcha ele Jarvis try korbe, na parle tumi browser-e solve koro.
- **Password safe:** password chat-e dio na, memory-teo save hoy na — login-er somoy ekbar use kore bhule jabe. Email Settings > Website Login-e bosale protibar ar chaibe na.

## v1.3.6 — Video, APK Build, Photo Edit, Shopping + Auth Removed

- **Auth delete kora hoyeche:** Face / fingerprint / password (PIN) authentication **sob delete** — Jarvis ekhon kono lock charai direct khulbe. Screen lock/wake (Device Admin) ache, kintu app khulte ar kichu lagbe na.
- **Nije nije video banate pare:** **"Video banao"** bollei Jarvis photo diye video banabe (slideshow MP4, Gallery > Movies > JarvisAI) — text overlay-o dite parbe.
- **Video edit (CapCut / YouCut):** **"Edit video"** bollei video CapCut-e (na thakle YouCut-e) khulbe — professional edit okhane koro.
- **Photo edit + background remove:** **"Edit photo"** (Snapseed/system editor), **"Background remove koro"** (transparent PNG, Gallery > Pictures > JarvisAI) — remove.bg key thakle Jarvis nijei korbe, na hole browser-e khule debe.
- **Gradle + Kotlin diye APK banate pare:** **"Build APK"** bollei Jarvis GitHub Actions-e (free) Gradle build chalabe ar toiri APK Downloads-e ene debe. Phone-e Gradle chole na — build GitHub-er server-e hoy.
- **Website control + publish:** ager motoi — GitHub Pages / Render deploy / browser diye tap kore publish.
- **Free domain + online order:** **"Free domain chai"** bollei free subdomain-er option khulbe. **"Flipkart/Amazon-e ... kino"** bollei product khuje cart obdhi niye jabe — **payment-er age confirm korbe, OTP/UPI tomar hatei, Jarvis nije kokhono pay korbe na.**
- **Video upload:** background-e upload hoy (upload_video) — 1 millisecond-e upload sombhob na, speed file size ar net-er upor nirbhor kore.

## v1.3.5 — Owner Authentication + Turbo Speed + Self-Learning

- **Owner Authentication (fingerprint / face / voice):** Settings > Owner Authentication-e **Fingerprint / Face** ON korle Jarvis khulte phone-er fingerprint/face lagbe (lock screen-e auto prompt ashe). **Secret voice passphrase** bosale seta bollei Jarvis khulbe. Onno keu phone nileo Jarvis use korte parbe na. **Sotti kotha:** phone-er nijer lock screen kono app khulte pare na (Android-er niyom) — eta Jarvis app-er lock; phone khulte tomar nijer PIN/fingerprint-i lagbe.
- **Turbo Speed:** reply ekhon **streaming**-e ashe — Gemini lekha shuru korlei kotha dekhte pabe, puro reply-er jonno wait korte hobe na. Time / date / battery / screen lock / "tumi ke" — egulo **instant** (network charai).
- **Self-Learning:** user jokhon Jarvis-ke correct kore ("na, eta na", "bhul"), Jarvis seta **mone rakhe** — porer bar ar sei bhul korbe na. Jetai shikhao, mone thakbe. Notun feature (code) ekhono update hisebe ashe, kintu Jarvis-er buddhi ar memory nije nije bare.
- **Browser diye publish:** API key/token na thakleo Jarvis browser khule nije tap kore publish/deploy korte pare (open_browser + read_screen + tap_text).

## v1.3.4 — Render Deploy (API + screen touch)

- **Render-e deploy korte pare:** **"Render deploy koro"** bollei Jarvis Render-e manual deploy trigger korbe. **"Render status"** bolle latest deploy-er status (live / building / failed) ar live URL dekhabe.
- **API token kothay bosabe:** Settings > Hosting / Render-e — **API key** (dashboard.render.com → Account Settings → API Keys, `rnd_…` diye shuru), **Service ID** (service-er page-er URL-e thake, `srv-…` diye shuru). Token chat-e dio na, ekhanei bosao.
- **Deploy Hook-o cholbe:** key na thakle service → Settings → Deploy Hook theke hook URL bosaleo deploy hobe.
- **Screen touch koreo deploy korte pare:** Jarvis Render dashboard khule nije **Manual Deploy**-e tap korte pare — API-ta besi reliable, tai setai default.

## v1.3.3 — GitHub Client ID pre-filled + Device Flow error message

- **GitHub Client ID age thekei bosano:** Settings > Connect-e Client ID-er ghore tomar OAuth App-er Client ID age thekei lekha thakbe — ar bosate hobe na.
- **Device Flow ON korte hobe:** GitHub app page-e giye **Enable Device Flow** ON koro — na hole code asbe na. Jarvis ekhon thik error-ta dekhabe ("Device Flow OFF ache…") jate bujhte paro ki korte hobe.
- Tarpor **Authorize GitHub (one click)** chaple code dekhabe — github.com/device-e code bosalei connect.

## v1.3.2 — Call nije nije receive + GitHub one-click authorize + Multi-voice + Deploy

- **Call nije nije receive kore kotha bolbe:** Settings > Features-e **Auto-answer calls** ON thakle call ele Jarvis nijei receive korbe ar speaker-e kotha bolbe (greeting + message niye rakha). Age `acceptRingingCall()` sudhu default dialer hole kaj korto — ebar Accessibility diye phone-er **Answer button-e tap** korar fallback ache, tai beshirbhag phone-ei kaj korbe. Accessibility ON thakte hobe (Settings > Permissions). "Answer call" / "end call" bole dileo hobe.
- **GitHub one-click authorize (token copy-paste ar lagbe na):** Settings > Connect-e **Authorize GitHub (one click)** — ekta code dekhabe, github.com/device-e giye code-ta bosalei connect hoye jabe. Ekbar sudhu free OAuth App baniye tar Client ID bosate hobe (github.com/settings/developers → New OAuth App → Homepage URL: https://github.com). Tarpor theke deploy/publish cholbe.
- **Multi-voice background-e:** Voice ekhon 12-ta (Puck, Charon, Kore, Fenrir, Aoede, Leda, Orus, Zephyr, Callirrhoe, Autonoe, Erinome, Sulafat) — Settings > Voice-e bodlano jay, ba bole dileo hobe: **"voice change koro" / "meyeder voice dao"**. Live voice background service-e chole, screen off holeo thambe na.
- **Deploy / Hosting:** **"deploy koro"** bollei website GitHub Pages-e live hoye jabe (GitHub connect thakle). Ar **"host website"** bolle ei phone-ei Wi-Fi server cholbe.
- **Screen recording + kotha ek sathe:** kotha bolte bolte record korle mic busy thakle video-only record hobe (age fail hoye jeto).
- **Screen on thakbe:** Jarvis kotha bolle ba record korle screen nije theke off hobe na (30 min obdhi).

## v1.3.1 — Screen Recording (nije nije video banabe)

- **"Screen record koro"** bollei Jarvis phone-er screen-er video record korbe (MP4, 1080p, mic audio soho) — video save hobe **Gallery > Movies > JarvisAI**-te.
- **"30 second record koro"** — auto-stop hobe 30 second por; **"stop recording"** bolle ageo thamano jay.
- Header-er chip-e **🎬 Screen record koro**-teo tap korte paro.
- ⚠️ Prothombar Android nije ekta system dialog dekhabe — **"Start now"** tap korte hobe (eta Android-er niyom, kono app-i skip korte pare na). Tarpor theke Jarvis nijei record korbe.
- Screenshot (read_screen) ar live screen share age thekei chilo — ebar video recording-o jog holo.

## v1.3 — repeat fix + Girlfriend/Comedy Mode + Connect + AI API Keys

### Repeat fix (bar bar eki kotha ar bolbe na)
- Chat history-te eki message duibar save hoy na, purono duplicate history clean hoy.
- Jarvis-er system prompt-e anti-repeat rule: eki sentence duibar bolbe na, protibar notun kore bolbe.
- Voice hotword 3 second-er modhye duibar trigger hobe na.

### Mode — Normal / Girlfriend / Comedy (Settings > Mode, ba header-er chip-e tap)
- **Girlfriend Mode:** emotional, caring, "jaan" bole dakbe, tomar mood bujhe kotha bolbe.
- **Comedy Mode:** funny, jokes, hasi — kaj-o korbe, moja kore.
- **Normal:** smart assistant. Mode bodlale voice + text — dujagay-i kaj kore.

### Connect (Settings > Connect)
- **WhatsApp / Instagram:** notification access diye connect — button-e tap kore allow koro, tarpor Auto-reply ON koro.
- **GitHub:** token + owner/repo bosiye **Connect GitHub** — live check kore "Connected as <username> • repository <name>" dekhabe.
- **Browser / Camera:** ek tap-e in-app browser ba camera khulbe.

### AI API Keys (Settings > AI API Keys)
- **Gemini / OpenAI / Claude** — tin provider-er key + model bosano jay, provider bodlano jay. Phone control (tools) shudhu Gemini-te kaj kore; OpenAI/Claude diye smart text reply pabe. Live voice Gemini-tei.

## v1.2 — notun feature (ager sob kichu serokom-i ache, sudhu egulo add)

### Admin & App Lock (Settings > Admin & App Lock)
- **Enable Device Admin** → tarpor "Screen lock koro" bolle / Settings-e **Lock Screen Now** chaple phone-er screen sathe sathe lock hobe.
- **Set PIN** → Jarvis app-er nijer 4-8 digit PIN bosao, **Jarvis app lock ON** korle app khulte PIN lagbe.
- **Wake Screen** → screen jagabe.
- ⚠️ Sotti kotha: kono app-i phone-er PIN / password / fingerprint bypass kore phone unlock korte pare NA. Phone khulte tomar nijer PIN / fingerprint-i lagbe. Eta Android-er security, sob app-er jonnoi eki niyom.

### Disco Light (Settings > Disco Light)
- Background-e Jarvis cholle display-er dhare dhare rainbow disco light cholbe. Off korte switch bondho koro. "Display over other apps" permission lagbe.

### Browser / Google (in-app)
- "Google kholo", "Google-e X search koro", "browser kholo" → Google in-app browser-e khulbe, search result render hobe, result-e tap kore page khola jabe, Chrome-eo khola jabe.

### Call & WhatsApp (background)
- Age thekei chilo: call ele naam bole, auto-answer-e Jarvis speaker-e kotha bole, message niye rakhe. v1.2-e **Call greeting** Settings theke bodlano jay.
- WhatsApp auto-reply: Settings-e **Auto-reply template** bosale sei reply jabe, khali rakhle AI reply debe.

### Hosting / Publish (command-e)
- "publish index.html" → GitHub repo-te file upload kore GitHub Pages link debe. Age Settings-e GitHub token + owner/repo bosao.
- "host website" → ei phone-ei Wi-Fi-te website host hobe (http://phone-ip:8080), same Wi-Fi-er sobai khulte parbe. "host bondho koro" bolle stop hobe.

### Background Video Upload
- Settings > Pick & Upload Video, ba "video upload koro" → background-e upload cholbe, app chere geleo cholbe. Upload URL Settings-e bosate hobe. URL na thakle YouTube upload screen khulbe (YouTube silent background post korte dey na).
