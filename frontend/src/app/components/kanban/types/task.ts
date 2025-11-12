export interface Task {
  id: number;
  title: string;
  status: TaskStatus;
  description: string;
  priority?: 'Low' | 'Medium' | 'High';
  sprintId?: number;
  deadline?: string;
  peopleAssigned?: number[]; // array of member IDs
  comments?: TaskComment[];
}

export type TaskStatus = "Open" | "In Progress" | "Done";

// Comment associated with a task
export interface TaskComment {
  id: number;
  taskId: number;
  authorId: number;
  content: string;
  date: string;
}