export interface TrainingProgram {
  id: number;
  code: string;
  name: string;
  startDate: string;
  endDate: string;
  status: string;
}

/** Editable string fields of the form, without the id. */
export type TrainingProgramFields = Omit<TrainingProgram, 'id'>;

/** Payload submitted by the form: the editable fields plus an optional id. */
export type TrainingProgramFormState = TrainingProgramFields & { id: number | null };
