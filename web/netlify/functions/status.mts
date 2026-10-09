import type { Config } from "@netlify/functions";
import { serviceStatus } from "../../lib/server/ai";
import { json } from "../../lib/server/http";
export default async (req: Request) => {
  if (req.method !== "GET") return json({ error: "Method not allowed." }, 405);
  return json(serviceStatus((name) => Netlify.env.get(name)));
};
export const config: Config = { path: "/api/status" };
