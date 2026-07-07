import Page from "./page";
import {
  ProjectHomeGate,
  USER_SELECTED_EVENT_NAME
} from "@/components/projects/ProjectHomeGate";

export function PageTypeContract() {
  return <Page />;
}

export function ProjectHomeGateTypeContract() {
  return <ProjectHomeGate />;
}

export function UserSelectedEventNameContract() {
  return USER_SELECTED_EVENT_NAME satisfies "docbranch:user-selected";
}
