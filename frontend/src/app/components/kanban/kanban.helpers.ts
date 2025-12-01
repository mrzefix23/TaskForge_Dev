export class KanbanHelpers {
  static getPriorityLabel(priority: string): string {
    const labels: { [key: string]: string } = {
      'LOW': 'Basse',
      'MEDIUM': 'Moyenne',
      'HIGH': 'Haute'
    };
    return labels[priority] || priority;
  }

  static getStatusLabel(status: string): string {
    const labels: { [key: string]: string } = {
      'TODO': '📋 À Faire',
      'IN_PROGRESS': '⏳ En Cours',
      'DONE': '✅ Terminé'
    };
    return labels[status] || status;
  }

  static getTaskCountLabel(taskCount: number): string {
    if (taskCount === 0) return 'Aucune tâche';
    return `${taskCount} tâche${taskCount > 1 ? 's' : ''}`;
  }
}
