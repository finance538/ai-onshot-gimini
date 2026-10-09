export const utilities = [
  {
    id: "calculator",
    name: "Calculator",
    category: "Everyday",
    icon: "calculator",
    description: "Calculate with parentheses, powers, and basic arithmetic.",
    example: "(240 * 0.85) + 18",
  },
  {
    id: "units",
    name: "Unit converter",
    category: "Everyday",
    icon: "ruler",
    description: "Convert length, weight, temperature, and data units.",
    example: "12 km to mi",
  },
  {
    id: "word-count",
    name: "Text statistics",
    category: "Text",
    icon: "align",
    description: "Count words, characters, lines, and reading time.",
    example: "Paste your text here…",
  },
  {
    id: "uppercase",
    name: "Uppercase",
    category: "Text",
    icon: "type",
    description: "Convert text to UPPERCASE.",
    example: "Make this stand out",
  },
  {
    id: "lowercase",
    name: "Lowercase",
    category: "Text",
    icon: "type",
    description: "Convert text to lowercase.",
    example: "SIMPLIFY THIS TEXT",
  },
  {
    id: "titlecase",
    name: "Title case",
    category: "Text",
    icon: "type",
    description: "Capitalize the first letter of each word.",
    example: "a new beginning",
  },
  {
    id: "slug",
    name: "URL slug",
    category: "Text",
    icon: "link",
    description: "Make a clean, Unicode-friendly URL slug.",
    example: "My next big idea!",
  },
  {
    id: "dedupe",
    name: "Remove duplicate lines",
    category: "Text",
    icon: "list",
    description: "Remove repeated and empty lines, keeping original order.",
    example: "apple\norange\napple",
  },
  {
    id: "sort",
    name: "Sort lines",
    category: "Text",
    icon: "list",
    description: "Sort lines alphabetically with natural number ordering.",
    example: "item 12\nitem 2\nitem 1",
  },
  {
    id: "json-format",
    name: "JSON formatter",
    category: "Developer",
    icon: "braces",
    description: "Validate and pretty-print JSON.",
    example: '{"project":"OneShot","ready":true}',
  },
  {
    id: "json-minify",
    name: "JSON minifier",
    category: "Developer",
    icon: "braces",
    description: "Validate JSON and remove unnecessary whitespace.",
    example: '{ "name": "OneShot", "tools": 27 }',
  },
  {
    id: "csv-json",
    name: "CSV to JSON",
    category: "Developer",
    icon: "table",
    description: "Convert CSV with headers, quoted fields, and line breaks.",
    example: "name,role\nSalma,Designer\nOmar,Developer",
  },
  {
    id: "json-csv",
    name: "JSON to CSV",
    category: "Developer",
    icon: "table",
    description: "Convert an array of objects to spreadsheet-safe CSV.",
    example: '[{"name":"Salma","role":"Designer"}]',
  },
  {
    id: "base64-encode",
    name: "Base64 encoder",
    category: "Developer",
    icon: "binary",
    description: "Encode UTF-8 text, including Arabic and emoji.",
    example: "Hello, مرحباً",
  },
  {
    id: "base64-decode",
    name: "Base64 decoder",
    category: "Developer",
    icon: "binary",
    description: "Decode Base64 back into UTF-8 text.",
    example: "SGVsbG8=",
  },
  {
    id: "url-encode",
    name: "URL encoder",
    category: "Developer",
    icon: "link",
    description: "Encode text for a URL parameter.",
    example: "hello world & مرحبا",
  },
  {
    id: "url-decode",
    name: "URL decoder",
    category: "Developer",
    icon: "link",
    description: "Decode a percent-encoded URL component.",
    example: "hello%20world",
  },
  {
    id: "html-encode",
    name: "HTML escaper",
    category: "Developer",
    icon: "code",
    description: "Escape markup into safe HTML entities.",
    example: "<h1>Hello & welcome</h1>",
  },
  {
    id: "html-decode",
    name: "HTML entity decoder",
    category: "Developer",
    icon: "code",
    description: "Decode common and numeric entities into plain text.",
    example: "&lt;h1&gt;Hello&lt;/h1&gt;",
  },
  {
    id: "sha256",
    name: "SHA-256 hash",
    category: "Security",
    icon: "shield",
    description: "Create a one-way SHA-256 text fingerprint.",
    example: "Text to fingerprint",
  },
  {
    id: "uuid",
    name: "UUID generator",
    category: "Security",
    icon: "fingerprint",
    description: "Generate 1–50 cryptographically random UUIDs.",
    example: "5",
  },
  {
    id: "password",
    name: "Password generator",
    category: "Security",
    icon: "key",
    description: "Generate a random password. Enter a length from 12–128.",
    example: "24",
  },
  {
    id: "epoch",
    name: "Timestamp converter",
    category: "Everyday",
    icon: "clock",
    description: "Convert Unix seconds, milliseconds, or an ISO date to UTC.",
    example: "1700000000",
  },
  {
    id: "color",
    name: "Color converter",
    category: "Design",
    icon: "palette",
    description: "Convert a 3- or 6-digit hex color to RGB and HSL.",
    example: "#e87850",
  },
  {
    id: "markdown",
    name: "Markdown preview",
    category: "Writing",
    icon: "file",
    description: "Preview headings, tables, lists, and code safely.",
    example: "# A fresh start\n\n- Write something useful\n- Make it clear",
  },
  {
    id: "qr",
    name: "QR code maker",
    category: "Design",
    icon: "qr",
    description: "Create and download a QR code for text or a link.",
    example: "https://1shotcam.com",
  },
  {
    id: "image",
    name: "Image resizer",
    category: "Design",
    icon: "image",
    description: "Resize and compress a JPG, PNG, or WebP on your device.",
    example: "",
  },
] as const;
export type UtilityId = (typeof utilities)[number]["id"];

export function calculate(source: string): number {
  const input = source.replace(/\s/g, "");
  if (input.length > 500 || !input)
    throw new Error("Enter a calculation of up to 500 characters.");
  const tokens =
    input.match(/(?:\d+(?:\.\d*)?|\.\d+)(?:e[+-]?\d+)?|[()+\-*/%^]/gi) || [];
  if (tokens.join("") !== input)
    throw new Error("Use numbers, parentheses, +, −, *, /, %, or ^.");
  let pos = 0;
  function atom(): number {
    const token = tokens[pos++];
    if (token === "(") {
      const value = sum();
      if (tokens[pos++] !== ")") throw new Error("Close every parenthesis.");
      return value;
    }
    if (!token || !/^(?:\d|\.)/.test(token))
      throw new Error("A number is missing.");
    return Number(token);
  }
  function power(): number {
    const a = atom();
    if (tokens[pos] === "^") {
      pos++;
      return a ** unary();
    }
    return a;
  }
  function unary(): number {
    if (tokens[pos] === "+") {
      pos++;
      return unary();
    }
    if (tokens[pos] === "-") {
      pos++;
      return -unary();
    }
    return power();
  }
  function product(): number {
    let value = unary();
    while (["*", "/", "%"].includes(tokens[pos])) {
      const op = tokens[pos++];
      const b = unary();
      value = op === "*" ? value * b : op === "/" ? value / b : value % b;
    }
    return value;
  }
  function sum(): number {
    let value = product();
    while (["+", "-"].includes(tokens[pos])) {
      const op = tokens[pos++];
      const b = product();
      value = op === "+" ? value + b : value - b;
    }
    return value;
  }
  const value = sum();
  if (pos !== tokens.length)
    throw new Error("Check the operators and parentheses.");
  if (!Number.isFinite(value))
    throw new Error(
      "The calculation has no finite result. Check for division by zero.",
    );
  return Number(value.toPrecision(14));
}

export function parseCsv(input: string): string[][] {
  const rows: string[][] = [];
  let row: string[] = [],
    field = "",
    quoted = false,
    closed = false;
  input = input.replace(/^\uFEFF/, "");
  for (let i = 0; i < input.length; i++) {
    const c = input[i];
    if (quoted) {
      if (c === '"' && input[i + 1] === '"') {
        field += '"';
        i++;
      } else if (c === '"') {
        quoted = false;
        closed = true;
      } else field += c;
    } else if (c === "," || c === "\r" || c === "\n") {
      row.push(field);
      field = "";
      closed = false;
      if (c !== ",") {
        rows.push(row);
        row = [];
        if (c === "\r" && input[i + 1] === "\n") i++;
      }
    } else if (c === '"' && !field && !closed) quoted = true;
    else {
      if (closed || c === '"')
        throw new Error("Invalid quote in CSV. Quote the entire field.");
      field += c;
    }
  }
  if (quoted) throw new Error("A quoted CSV field is not closed.");
  if (field || row.length || closed) {
    row.push(field);
    rows.push(row);
  }
  return rows;
}

function convertUnits(input: string): string {
  const match = input
    .trim()
    .match(/^([+-]?(?:\d+(?:\.\d*)?|\.\d+))\s*([a-z]+)\s+to\s+([a-z]+)$/i);
  if (!match)
    throw new Error(
      "Use a value and units, for example: 12 km to mi or 25 c to f.",
    );
  const [, raw, fromRaw, toRaw] = match;
  const from = fromRaw.toLowerCase(),
    to = toRaw.toLowerCase(),
    amount = Number(raw);
  const units: Record<string, [string, number]> = {
    mm: ["length", 0.001],
    cm: ["length", 0.01],
    m: ["length", 1],
    km: ["length", 1000],
    in: ["length", 0.0254],
    ft: ["length", 0.3048],
    yd: ["length", 0.9144],
    mi: ["length", 1609.344],
    mg: ["mass", 0.000001],
    g: ["mass", 0.001],
    kg: ["mass", 1],
    lb: ["mass", 0.45359237],
    oz: ["mass", 0.028349523125],
    b: ["data", 1],
    kb: ["data", 1000],
    mb: ["data", 1e6],
    gb: ["data", 1e9],
    kib: ["data", 1024],
    mib: ["data", 1048576],
    gib: ["data", 1073741824],
  };
  let result: number;
  if (["c", "f", "k"].includes(from) && ["c", "f", "k"].includes(to)) {
    const celsius =
      from === "c"
        ? amount
        : from === "f"
          ? ((amount - 32) * 5) / 9
          : amount - 273.15;
    if (celsius < -273.15)
      throw new Error("Temperature cannot be below absolute zero.");
    result =
      to === "c"
        ? celsius
        : to === "f"
          ? (celsius * 9) / 5 + 32
          : celsius + 273.15;
  } else {
    if (!units[from] || !units[to] || units[from][0] !== units[to][0])
      throw new Error(
        "Use compatible length (mm–mi), weight (mg–lb), temperature (c/f/k), or byte (b–gib) units.",
      );
    result = (amount * units[from][1]) / units[to][1];
  }
  if (!Number.isFinite(result)) throw new Error("The number is too large.");
  return `${raw} ${from} = ${Number(result.toPrecision(12))} ${to}`;
}

export async function runUtility(
  id: UtilityId,
  input: string,
): Promise<string> {
  if (!input.trim()) throw new Error("Enter some input first.");
  if (input.length > 100_000)
    throw new Error("Keep input under 100,000 characters.");
  switch (id) {
    case "calculator":
      return String(calculate(input));
    case "units":
      return convertUnits(input);
    case "uppercase":
      return input.toLocaleUpperCase();
    case "lowercase":
      return input.toLocaleLowerCase();
    case "titlecase":
      return input
        .toLocaleLowerCase()
        .replace(
          /(^|\s)(\p{L})/gu,
          (_, space: string, letter: string) =>
            space + letter.toLocaleUpperCase(),
        );
    case "slug":
      return input
        .normalize("NFKD")
        .replace(/\p{M}/gu, "")
        .toLowerCase()
        .trim()
        .replace(/[^\p{L}\p{N}]+/gu, "-")
        .replace(/^-|-$/g, "");
    case "dedupe":
      return [
        ...new Set(
          input
            .split(/\r?\n/)
            .map((s) => s.trim())
            .filter(Boolean),
        ),
      ].join("\n");
    case "sort":
      return input
        .split(/\r?\n/)
        .sort((a, b) => a.localeCompare(b, undefined, { numeric: true }))
        .join("\n");
    case "json-format":
      return JSON.stringify(JSON.parse(input), null, 2);
    case "json-minify":
      return JSON.stringify(JSON.parse(input));
    case "csv-json": {
      const [headers, ...rows] = parseCsv(input);
      if (
        !headers?.length ||
        headers.some((h) => !h.trim()) ||
        new Set(headers).size !== headers.length
      )
        throw new Error("CSV headers must be non-empty and unique.");
      if (rows.some((row) => row.length !== headers.length))
        throw new Error(
          "Each CSV row must have the same number of fields as the header.",
        );
      return JSON.stringify(
        rows.map((row) =>
          Object.fromEntries(headers.map((h, i) => [h, row[i]])),
        ),
        null,
        2,
      );
    }
    case "json-csv": {
      const rows: unknown = JSON.parse(input);
      if (
        !Array.isArray(rows) ||
        !rows.length ||
        rows.some(
          (row) => !row || typeof row !== "object" || Array.isArray(row),
        )
      )
        throw new Error("Supply a non-empty JSON array of objects.");
      const headers = [...new Set(rows.flatMap((row) => Object.keys(row)))];
      const cell = (value: unknown) => {
        let text =
          value == null
            ? ""
            : typeof value === "object"
              ? JSON.stringify(value)
              : String(value);
        if (/^[\s]*[=+\-@]/.test(text)) text = `'${text}`;
        return `"${text.replace(/"/g, '""')}"`;
      };
      return [
        headers.map(cell).join(","),
        ...rows.map((row) => headers.map((h) => cell(row[h])).join(",")),
      ].join("\r\n");
    }
    case "base64-encode":
      return btoa(
        Array.from(new TextEncoder().encode(input), (b) =>
          String.fromCharCode(b),
        ).join(""),
      );
    case "base64-decode":
      return new TextDecoder("utf-8", { fatal: true }).decode(
        Uint8Array.from(atob(input.trim()), (c) => c.charCodeAt(0)),
      );
    case "url-encode":
      return encodeURIComponent(input);
    case "url-decode":
      return decodeURIComponent(input);
    case "html-encode":
      return input.replace(
        /[&<>"']/g,
        (c) =>
          ({
            "&": "&amp;",
            "<": "&lt;",
            ">": "&gt;",
            '"': "&quot;",
            "'": "&#39;",
          })[c]!,
      );
    case "html-decode":
      return input.replace(
        /&(#x[\da-f]+|#\d+|amp|lt|gt|quot|apos|nbsp);/gi,
        (entity, code: string) => {
          if (code.startsWith("#")) {
            const n =
              code[1].toLowerCase() === "x"
                ? parseInt(code.slice(2), 16)
                : Number(code.slice(1));
            return n >= 0 && n <= 0x10ffff ? String.fromCodePoint(n) : entity;
          }
          return (
            {
              amp: "&",
              lt: "<",
              gt: ">",
              quot: '"',
              apos: "'",
              nbsp: "\u00a0",
            }[code.toLowerCase()] || entity
          );
        },
      );
    case "word-count": {
      const words = input.trim().split(/\s+/u).length;
      return `${words} words\n${Array.from(input).length} characters\n${Array.from(input.replace(/\s/g, "")).length} characters without spaces\n${input.split(/\r?\n/).length} lines\n${Math.max(1, Math.ceil(words / 200))} min estimated reading time`;
    }
    case "sha256":
      return Array.from(
        new Uint8Array(
          await crypto.subtle.digest(
            "SHA-256",
            new TextEncoder().encode(input),
          ),
        ),
        (b) => b.toString(16).padStart(2, "0"),
      ).join("");
    case "uuid": {
      const count = Number(input);
      if (!Number.isInteger(count) || count < 1 || count > 50)
        throw new Error("Enter a whole number from 1 to 50.");
      return Array.from({ length: count }, () => crypto.randomUUID()).join(
        "\n",
      );
    }
    case "password": {
      const length = Number(input);
      if (!Number.isInteger(length) || length < 12 || length > 128)
        throw new Error("Choose a length from 12 to 128.");
      const alphabet =
        "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%&*+-=?";
      let password = "";
      // Rejection sampling avoids modulo bias.
      while (password.length < length) {
        const bytes = crypto.getRandomValues(new Uint8Array(length));
        for (const b of bytes)
          if (b < 256 - (256 % alphabet.length) && password.length < length)
            password += alphabet[b % alphabet.length];
      }
      return password;
    }
    case "epoch": {
      const value = input.trim();
      if (
        !/^[+-]?\d+$/.test(value) &&
        !/^\d{4}-\d{2}-\d{2}T.*(?:Z|[+-]\d{2}:\d{2})$/i.test(value)
      )
        throw new Error(
          "Enter Unix seconds/milliseconds or an ISO date with timezone, such as 2026-01-01T12:00:00Z.",
        );
      const date = /^[+-]?\d+$/.test(value)
        ? new Date(
            Number(value) *
              (value.replace(/^[+-]/, "").length <= 10 ? 1000 : 1),
          )
        : new Date(value);
      if (!Number.isFinite(date.getTime()))
        throw new Error("Enter a valid timestamp or ISO date.");
      return `UTC: ${date.toISOString()}\nUnix seconds: ${Math.floor(date.getTime() / 1000)}\nUnix milliseconds: ${date.getTime()}`;
    }
    case "color": {
      let hex = input.trim().replace(/^#/, "");
      if (!/^([\da-f]{3}|[\da-f]{6})$/i.test(hex))
        throw new Error("Enter a 3- or 6-digit hex color, such as #e87850.");
      if (hex.length === 3)
        hex = hex
          .split("")
          .map((c) => c + c)
          .join("");
      const rgb = [0, 2, 4].map((i) => parseInt(hex.slice(i, i + 2), 16));
      const [r, g, b] = rgb.map((n) => n / 255),
        max = Math.max(r, g, b),
        min = Math.min(r, g, b),
        d = max - min,
        l = (max + min) / 2;
      let h = 0;
      if (d)
        h =
          (max === r
            ? ((g - b) / d) % 6
            : max === g
              ? (b - r) / d + 2
              : (r - g) / d + 4) * 60;
      return `#${hex.toUpperCase()}\nrgb(${rgb.join(", ")})\nhsl(${Math.round((h + 360) % 360)}, ${Math.round((d ? d / (1 - Math.abs(2 * l - 1)) : 0) * 100)}%, ${Math.round(l * 100)}%)`;
    }
    case "markdown":
      return input;
    default:
      throw new Error("Use the dedicated image or QR controls.");
  }
}
