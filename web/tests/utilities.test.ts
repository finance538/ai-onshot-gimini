import test from "node:test";
import assert from "node:assert/strict";
import { calculate, parseCsv, runUtility, utilities } from "../lib/utilities";

test("calculator handles precedence, negative powers, and nested expressions without eval", () => {
  assert.equal(calculate("(240 * 0.85) + 18"), 222);
  assert.equal(calculate("2^3^2"), 512);
  assert.equal(calculate("-2^2"), -4);
  assert.equal(calculate("2^-2"), 0.25);
  assert.equal(calculate("1e3 / (2 + 3)"), 200);
  for (const value of [
    "1/0",
    "1 +",
    "2(3)",
    "(1 + 2",
    "globalThis.alert(1)",
    "1;2",
  ])
    assert.throws(() => calculate(value));
});
test("CSV parser preserves quoted commas, escaped quotes, CRLF, and embedded newlines", async () => {
  assert.deepEqual(
    parseCsv('name,notes\r\n"Salma, A.","line 1\nline ""2"""\r\n'),
    [
      ["name", "notes"],
      ["Salma, A.", 'line 1\nline "2"'],
    ],
  );
  assert.throws(() => parseCsv('a,b\n"unclosed,x'));
  assert.throws(() => parseCsv('a,b\n"value"oops,x'));
  await assert.rejects(runUtility("csv-json", "a,a\n1,2"));
  await assert.rejects(runUtility("csv-json", "a,b\n1,2,3"));
  const result = JSON.parse(
    await runUtility("csv-json", "__proto__,name\na,b"),
  );
  assert.equal(result[0].__proto__, "a");
});
test("JSON-to-CSV handles nested values and neutralizes spreadsheet formulas", async () => {
  const csv = await runUtility(
    "json-csv",
    '[{"name":"=HYPERLINK(1)","other":{"a":1}},{"name":"hello, world","extra":true}]',
  );
  const rows = parseCsv(csv);
  assert.deepEqual(rows[0], ["name", "other", "extra"]);
  assert.equal(rows[1][0], "'=HYPERLINK(1)");
  assert.equal(rows[1][1], '{"a":1}');
  assert.equal(rows[2][0], "hello, world");
});
test("encoding round trips Unicode text and rejects malformed data", async () => {
  const input = "مرحبا 🌍 & <hello>";
  assert.equal(
    await runUtility("base64-decode", await runUtility("base64-encode", input)),
    input,
  );
  assert.equal(
    await runUtility("url-decode", await runUtility("url-encode", input)),
    input,
  );
  assert.equal(
    await runUtility("html-decode", await runUtility("html-encode", input)),
    input,
  );
  await assert.rejects(runUtility("base64-decode", "not-base64!"));
  await assert.rejects(runUtility("url-decode", "%E0%A4"));
});
test("unit conversions reject incompatible units and impossible temperatures", async () => {
  assert.equal(await runUtility("units", "1 mi to km"), "1 mi = 1.609344 km");
  assert.equal(await runUtility("units", "32 f to c"), "32 f = 0 c");
  assert.equal(await runUtility("units", "1 mib to b"), "1 mib = 1048576 b");
  await assert.rejects(runUtility("units", "4 kg to km"));
  await assert.rejects(runUtility("units", "-1 k to c"));
});
test("SHA-256 matches a known vector, IDs are unique, and password lengths are bounded", async () => {
  assert.equal(
    await runUtility("sha256", "abc"),
    "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
  );
  const ids = (await runUtility("uuid", "50")).split("\n");
  assert.equal(new Set(ids).size, 50);
  assert.ok(ids.every((id) => /^[0-9a-f-]{36}$/.test(id)));
  const password = await runUtility("password", "24");
  assert.equal(password.length, 24);
  await assert.rejects(runUtility("password", "0"));
  await assert.rejects(runUtility("uuid", "9999"));
});
test("timestamps, color conversion, and text operations return meaningful results", async () => {
  assert.match(
    await runUtility("epoch", "1700000000"),
    /2023-11-14T22:13:20.000Z/,
  );
  assert.match(await runUtility("color", "#fff"), /rgb\(255, 255, 255\)/);
  assert.equal(await runUtility("dedupe", "a\na\nb\n"), "a\nb");
  assert.equal(await runUtility("sort", "item 12\nitem 2"), "item 2\nitem 12");
  assert.equal(await runUtility("slug", "مرحبا بالعالم!"), "مرحبا-بالعالم");
});
test("every text utility has a working example and input limits", async () => {
  for (const tool of utilities.filter(
    (tool) => !["image", "qr"].includes(tool.id),
  )) {
    assert.ok((await runUtility(tool.id, tool.example)).length > 0, tool.id);
  }
  await assert.rejects(runUtility("uppercase", "a".repeat(100001)));
});
