import { CommonModule, TitleCasePipe } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Task, TaskStatus } from './types/task';

@Component({
  selector: 'app-kanban',
  standalone: true,
  imports: [CommonModule, FormsModule, TitleCasePipe],
  templateUrl: './kanban.html',
  styleUrls: ['./kanban.css'],
})
export class Kanban {
  tasks: Task[] = [
    { id: 1, title: 'Design layout', status: 'Open', description: 'Create wireframes for the dashboard UI' },
    { id: 2, title: 'Implement backend', status: 'In Progress', description: 'Set up REST APIs for task management' },
    { id: 3, title: 'Write unit tests', status: 'Done', description: 'Add test cases for authentication module' },
    { id: 4, title: 'Create login screen', status: 'Open', description: 'Build responsive login form with validations' },
    { id: 5, title: 'Integrate database', status: 'In Progress', description: 'Connect to MongoDB and model schemas' },
    { id: 6, title: 'Setup CI/CD pipeline', status: 'Done', description: 'Deploy app automatically with GitHub Actions' },
  ];

  getTaskByStatus(status: string) {
    return this.tasks.filter(it => it.status === status)
  }

  // Editing state
  editingTaskId: number | null = null;
  editedDescription = '';

  startEdit(task: Task) {
    this.editingTaskId = task.id;
    // we only edit the description now (no separate title)
    this.editedDescription = task.description;
    // focus the single-line input after the view updates
    setTimeout(() => {
      const el = document.getElementById(`edit-input-${task.id}`) as HTMLInputElement | null;
      if (el) {
        el.focus();
        el.select();
      }
    }, 0);
  }

  addTask(status: string) {
    // find max id
    const maxId = this.tasks.reduce((m, t) => Math.max(m, t.id), 0);
    const newTask: Task = {
      id: maxId + 1,
      title: '',
      status: status as TaskStatus,
      description: ''
    };
    this.tasks.unshift(newTask);
    // open the new task for editing in the column where it was added
    this.startEdit(newTask);
  }

  saveEdit(task: Task) {
    if (!this.editingTaskId) return;
    const t = this.tasks.find(x => x.id === task.id);
    if (!t) return;
    // update only description (titles removed from display)
    t.description = this.editedDescription.trim();
    this.editingTaskId = null;
  }

  cancelEdit() {
    this.editingTaskId = null;
  }

  onDrop(event: DragEvent, status: string) {
    event.preventDefault();
    const taskId = Number(event.dataTransfer?.getData("text"));
    const task = this.tasks.find(t => t.id === taskId);
    if (task) {
      task.status = status as TaskStatus;
    }

    const draggingEl = document.querySelector(".dragging");
    if (draggingEl) draggingEl.classList.remove("dragging");
  }

  onDragStart(event: DragEvent, task: Task) {
    // prevent dragging when editing this task
    if (this.editingTaskId !== null && this.editingTaskId === task.id) {
      event.preventDefault();
      return;
    }
    event.dataTransfer?.setData("text", task.id.toString());
    (event.target as HTMLElement).classList.add("dragging");
  }

  onDragEnd(event: DragEvent) {
    (event.target as HTMLElement).classList.remove("dragging");
  }

}
