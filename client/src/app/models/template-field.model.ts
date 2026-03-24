export interface TemplateField {
  key: string;
  label: string;
  type: 'text' | 'number' | 'date';
  required: boolean;
  defaultValue?: unknown;
}
