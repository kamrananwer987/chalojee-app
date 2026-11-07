// Renders chaloje.com in a phone-sized browser and records links, text and screenshots.
import { chromium, devices } from 'playwright';
import fs from 'fs';

const out = process.argv[2];
const browser = await chromium.launch();
const ctx = await browser.newContext({ ...devices['Pixel 7'] });
const page = await ctx.newPage();

const visited = new Set();
const queue = ['https://chaloje.com/'];
const report = [];

while (queue.length && visited.size < 8) {
  const url = queue.shift();
  if (visited.has(url)) continue;
  visited.add(url);
  try {
    await page.goto(url, { waitUntil: 'networkidle', timeout: 45000 });
  } catch (e) { report.push({ url, error: String(e) }); continue; }
  await page.waitForTimeout(2000);
  const n = visited.size;
  await page.screenshot({ path: `${out}/page${n}.png`, fullPage: false });
  const data = await page.evaluate(() => {
    const links = [...document.querySelectorAll('a[href]')].map(a => ({
      text: (a.innerText || a.getAttribute('aria-label') || a.title || '').trim().slice(0, 60),
      href: a.href,
    }));
    const css = getComputedStyle(document.body);
    const header = document.querySelector('header, nav, .navbar');
    return {
      title: document.title,
      finalUrl: location.href,
      text: document.body.innerText.slice(0, 3000),
      links,
      bodyBg: css.backgroundColor,
      headerBg: header ? getComputedStyle(header).backgroundColor : null,
      buttons: [...document.querySelectorAll('button, .btn')].slice(0, 15)
        .map(b => ({ text: b.innerText.trim().slice(0, 40), bg: getComputedStyle(b).backgroundColor })),
    };
  });
  report.push({ url, n, ...data });
  for (const l of data.links) {
    try {
      const u = new URL(l.href);
      if (u.hostname.endsWith('chaloje.com') && !u.hash && !visited.has(u.href)) queue.push(u.href);
    } catch {}
  }
}

fs.writeFileSync(`${out}/report.json`, JSON.stringify(report, null, 2));
await browser.close();
console.log('pages:', [...visited]);
