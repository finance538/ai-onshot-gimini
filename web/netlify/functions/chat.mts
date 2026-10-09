import type { Config } from "@netlify/functions";
import { completeChat } from "../../lib/server/ai";
import { errorResponse, json, readJson } from "../../lib/server/http";
export default async (req: Request) => {
  if (req.method !== "POST") return json({ error: "Method not allowed." }, 405);
  try {
    return json(
      await completeChat(
        await readJson(req),
        (name) => Netlify.env.get(name),
        req.signal,
      ),
    );
  } catch (error) {
    return errorResponse(error);
  }
};
export const config: Config = {
  path: "/api/chat",
  rateLimit: { windowLimit: 20, windowSize: 60, aggregateBy: ["ip"] },
};
