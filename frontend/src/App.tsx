import { useState } from 'react';
import ProgramForm from './components/ProgramForm';
import ProgramList from './components/ProgramList';
import { TrainingProgram, TrainingProgramFormState } from './types';

const API_URL = 'http://localhost:8080/programs';

function App() {
  const [programs, setPrograms] = useState<TrainingProgram[]>([]);
  const [editingProgram, setEditingProgram] = useState<TrainingProgram | null>(null);

  const loadPrograms = async (): Promise<void> => {
    const res = await fetch(API_URL);
    const data: TrainingProgram[] = await res.json();
    setPrograms(data);
  };

  const handleCreateProgram = async (newProgram: TrainingProgramFormState): Promise<void> => {
    await fetch(API_URL, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(newProgram),
    });
    await loadPrograms();
  };

  const handleUpdateProgram = async (updatedProgram: TrainingProgramFormState): Promise<void> => {
    await fetch(`${API_URL}/${updatedProgram.id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(updatedProgram),
    });
    await loadPrograms();
    setEditingProgram(null); // Clear the form after updating
  };

  return (
    <div style={{ padding: '2rem' }}>
      <ProgramForm
        onSubmit={editingProgram ? handleUpdateProgram : handleCreateProgram}
        editingProgram={editingProgram}
      />
      <ProgramList
        programs={programs}
        onEdit={setEditingProgram}
        refresh={loadPrograms}
      />
    </div>
  );
}

export default App;
