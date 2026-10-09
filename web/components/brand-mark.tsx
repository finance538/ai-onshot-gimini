const BLADES = [
  "M 389.87 216.11 A 140 140 0 0 1 325.22 328.07 Q 327.87 283.90 301.18 238.95 A 58.8 58.8 0 0 0 301.18 181.05 Q 354.39 184.94 389.87 216.11 Z",
  "M 314.64 334.18 A 140 140 0 0 1 185.36 334.18 Q 224.94 314.39 250.51 268.80 A 58.8 58.8 0 0 0 300.66 239.84 Q 323.90 287.87 314.64 334.18 Z",
  "M 174.78 328.07 A 140 140 0 0 1 110.13 216.11 Q 147.07 240.49 199.34 239.84 A 58.8 58.8 0 0 0 249.49 268.80 Q 219.51 312.93 174.78 328.07 Z",
  "M 110.13 203.89 A 140 140 0 0 1 174.78 91.93 Q 172.13 136.10 198.82 181.05 A 58.8 58.8 0 0 0 198.82 238.95 Q 145.61 235.06 110.13 203.89 Z",
  "M 185.36 85.82 A 140 140 0 0 1 314.64 85.82 Q 275.06 105.61 249.49 151.20 A 58.8 58.8 0 0 0 199.34 180.16 Q 176.10 132.13 185.36 85.82 Z",
  "M 325.22 91.93 A 140 140 0 0 1 389.87 203.89 Q 352.93 179.51 300.66 180.16 A 58.8 58.8 0 0 0 250.51 151.20 Q 280.49 107.07 325.22 91.93 Z",
];

/** oneShot emblem: aperture blades follow the theme, reticle stays Precision Orange. */
export function BrandMark({
  size = 32,
  title,
}: {
  size?: number;
  title?: string;
}) {
  return (
    <svg
      className="brand-mark"
      viewBox="96 56 308 308"
      width={size}
      height={size}
      role={title ? "img" : undefined}
      aria-label={title}
      aria-hidden={title ? undefined : true}
    >
      {BLADES.map((d) => (
        <path key={d} d={d} fill="var(--logo-blade)" />
      ))}
      <g fill="#FF5500">
        <circle
          cx="250"
          cy="210"
          r="44.69"
          fill="none"
          stroke="#FF5500"
          strokeWidth="3.8"
        />
        <rect x="247.9" y="157.31" width="4.2" height="16" rx="1" />
        <rect x="247.9" y="246.69" width="4.2" height="16" rx="1" />
        <rect x="197.31" y="207.9" width="16" height="4.2" rx="1" />
        <rect x="286.69" y="207.9" width="16" height="4.2" rx="1" />
        <circle cx="250" cy="210" r="16.98" />
      </g>
    </svg>
  );
}
