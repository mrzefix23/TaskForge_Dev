import { CommonModule, TitleCasePipe } from '@angular/common';
import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Task, TaskStatus } from './types/task';

@Component({
  selector: 'app-kanban',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './kanban.html',
  styleUrls: ['./kanban.css'],
})
export class Kanban {
  tasks: Task[] = [
    { id: 1, title: 'Design layout', status: 'Open', description: 'Create wireframes for the dashboard UI', priority: 'High', sprintId: 1 },
    { id: 2, title: 'Implement backend', status: 'In Progress', description: 'Set up REST APIs for task management' , priority: 'Medium', sprintId: 1 },
    { id: 3, title: 'Write unit tests', status: 'Done', description: 'Add test cases for authentication module' , priority: 'Low', sprintId: 2 },
    { id: 4, title: 'Create login screen', status: 'Open', description: 'Build responsive login form with validations' , priority: 'High', sprintId: 2 },
    { id: 5, title: 'Integrate database', status: 'In Progress', description: 'Connect to MongoDB and model schemas', priority: 'Medium', sprintId: 1 },
    { id: 6, title: 'Setup CI/CD pipeline', status: 'Done', description: 'Deploy app automatically with GitHub Actions' , priority: 'Low', sprintId: 3 },
  ];

  // Filters
  selectedPriority: 'High' | 'Medium' | 'Low' | '' = '';
  selectedSprint: string = '';

  // Get tasks by status with applied filters
  getTaskByStatus(status: string) {
    return this.tasks
      .filter(t => t.status === status)
      .filter(t => !this.selectedPriority || t.priority === this.selectedPriority)
      .filter(t => !this.selectedSprint || t.sprintId === Number(this.selectedSprint));
  }

  // columns configuration (id is the status value stored on tasks, label is the editable name)
  columns: Array<{ id: string; label: string }> = [
    { id: 'Open', label: 'Open' },
    { id: 'In Progress', label: 'In Progress' },
    { id: 'Done', label: 'Done' },
  ];

  // column edit state
  editingColumnId: string | null = null;
  editedColumnLabel = '';

  // Editing state
  editingTaskId: number | null = null;
  editedDescription = '';
  // context menu state
  contextMenuVisible = false;
  contextMenuX = 0;
  contextMenuY = 0;
  contextMenuTaskId: number | null = null;

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

  // column operations
  startEditColumn(col: { id: string; label: string }) {
    this.editingColumnId = col.id;
    this.editedColumnLabel = col.label;
    setTimeout(() => {
      const safeId = col.id.replace(/\s+/g, '-');
      const el = document.getElementById(`col-input-${safeId}`) as HTMLInputElement | null;
      if (el) {
        el.focus();
        el.select();
      }
    }, 0);
  }

  saveColumn(col: { id: string; label: string }) {
    if (!this.editingColumnId) return;
    const c = this.columns.find(x => x.id === col.id);
    if (!c) return;
    c.label = this.editedColumnLabel.trim() || c.label;
    this.editingColumnId = null;
  }

  cancelEditColumn() {
    this.editingColumnId = null;
  }

  addColumn() {
    const id = `col-${Date.now()}`;
    const newCol = { id, label: 'Nouvelle colonne' };
    this.columns.push(newCol);
    // open for editing
    setTimeout(() => this.startEditColumn(newCol), 0);
  }

  getSafeId(id: string) {
    return id.replace(/\s+/g, '-').replace(/[^a-zA-Z0-9-_]/g, '');
  }

  openContextMenu(event: MouseEvent, task: Task) {
    event.preventDefault();
    this.contextMenuTaskId = task.id;
    this.contextMenuX = event.clientX;
    this.contextMenuY = event.clientY;
    this.contextMenuVisible = true;
  }

  setPriority(task: Task, priority: 'Low' | 'Medium' | 'High') {
    const t = this.tasks.find(x => x.id === task.id);
    if (!t) return;
    t.priority = priority;
    this.contextMenuVisible = false;
  }

  deleteTask(task: Task) {
    this.tasks = this.tasks.filter(t => t.id !== task.id);
    this.contextMenuVisible = false;
    // if currently editing this task, cancel edit
    if (this.editingTaskId === task.id) this.cancelEdit();
  }

  // safer helpers that take an id (used by the template)
  setPriorityById(taskId: number | null, priority: 'Low' | 'Medium' | 'High') {
    if (taskId == null) return;
    const t = this.tasks.find(x => x.id === taskId);
    if (!t) return;
    t.priority = priority;
    this.closeContextMenu();
  }

  deleteTaskById(taskId: number | null) {
    if (taskId == null) return;
    this.tasks = this.tasks.filter(t => t.id !== taskId);
    if (this.editingTaskId === taskId) this.cancelEdit();
    this.closeContextMenu();
  }

  closeContextMenu() {
    this.contextMenuVisible = false;
    this.contextMenuTaskId = null;
  }

  private onWindowClick = (e: MouseEvent) => {
    if (this.contextMenuVisible) this.closeContextMenu();
  }

  private onKeyDown = (e: KeyboardEvent) => {
    if (e.key === 'Escape' && this.contextMenuVisible) this.closeContextMenu();
  }

  ngOnInit(): void {
    window.addEventListener('click', this.onWindowClick);
    window.addEventListener('keydown', this.onKeyDown);
  }

  ngOnDestroy(): void {
    window.removeEventListener('click', this.onWindowClick);
    window.removeEventListener('keydown', this.onKeyDown);
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
