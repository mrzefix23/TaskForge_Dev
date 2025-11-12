export interface Task {
  id: number;
  title: string;
  status: TaskStatus;
  description: string;
}

export type TaskStatus = "Open" | "In Progress" | "Done";