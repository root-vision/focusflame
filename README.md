# Focus Flame 🔥

Barcha telefon bildirishnomalarini Telegramga yuboruvchi Android ilova.

## Qanday ishlaydi
- Ilova **YOPIQ** bo'lsa ham ishlaydi
- Telefon o'chib yonsa ham **avtomatik** qayta ishlaydi
- Telegram, o'z xabarlari **yuborilmaydi** (loop yo'q)
- Pomodoro taymer + statistika

## Bot ma'lumotlari
- Bot: @FtestChat_bot
- Token: 8404558084:AAG33eFh_RQzdhJiC1-URlS0_pBFkvJetT0
- Chat ID: 5351720273

## APK qilish (GitHub Actions - BEPUL)

### 1. GitHub'ga yuklash
```
cd /home/neo/Desktop/FocusFlame
git init
git add .
git commit -m "Focus Flame v1.0"
```
GitHub'da yangi repo oching: https://github.com/new
```
git remote add origin https://github.com/SIZNING_USERNAME/focusflame.git
git push -u origin main
```

### 2. GitHub Actions workflow qo'shish
`.github/workflows/build.yml` fayli allaqachon tayyor.
Push qilgandan so'ng Actions tab → build → APK yuklab oling.

## O'rnatish
1. APK yuklab oling
2. Telefonda: Sozlamalar → Xavfsizlik → Noma'lum manbalar → Yoqish
3. APK o'rnating
4. Ilovani oching
5. "Notification Ruxsatini Berish" tugmasini bosing
6. Focus Flame yoniga yoqing
7. Tayyor! Endi barcha bildirishnomalar Telegramga ketadi
