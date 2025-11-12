export interface Task {
  id: number;
  title: string;
  status: TaskStatus;
  description: string;
  priority?: 'Low' | 'Medium' | 'High';
  sprintId?: number;
}

export type TaskStatus = "Open" | "In Progress" | "Done";