import { rootAuthLoader as clerkRootAuthLoader } from "@clerk/remix/ssr.server";
import type { LoaderFunction } from "@remix-run/node";

export const rootAuthLoader: LoaderFunction = (args) => {
  return clerkRootAuthLoader(args);
};
