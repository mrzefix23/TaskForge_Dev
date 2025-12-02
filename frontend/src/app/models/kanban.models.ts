export interface Project {
  id: number;
  name: string;
  description: string;
  owner: { username: string };
  members: { username: string }[];
}

export interface Sprint {
  id: number;
  name: string;
  startDate: string;
  endDate: string;
  status: 'PLANNED' | 'ACTIVE' | 'COMPLETED';
  projectId: number;
}

export interface KanbanColumn {
  id: number;
  name: string;
  status: string;
  order: number;
  projectId: number;
  isDefault: boolean;
}

export interface Task {
  id: number;
  title: string;
  description: string;
  priority: 'LOW' | 'MEDIUM' | 'HIGH';
  status: 'TODO' | 'IN_PROGRESS' | 'DONE';
  assignedTo?: { username: string };
  userStory: { id: number };
}

export interface UserStory {
  id: number;
  title: string;
  description: string;
  priority: 'LOW' | 'MEDIUM' | 'HIGH';
  status: string;
  assignedTo?: { username: string }[];
  sprint?: Sprint;
  tasks?: Task[];
  showTasks?: boolean;
  version?: Version;
  kanbanColumn?: KanbanColumn;
}

export interface Version {
  id: number;
  title: string;
  description: string;
  versionNumber: string;
  releaseDate?: string;
  status: 'PLANNED' | 'IN_PROGRESS' | 'RELEASED' | 'ARCHIVED';
  userStories?: UserStory[];
}

export type Priority = 'LOW' | 'MEDIUM' | 'HIGH';
export type Status = string;
