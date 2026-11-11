import test from 'node:test';
import assert from 'node:assert/strict';
import { motivoIndisponibilidade } from '../src/services/oportunidades.js';

test('disponibilidade considera vagas restantes, data e status', () => {
  const aberta = { status: 'ABERTA', data: '2027-01-01', vagas: 5, vagasDisponiveis: 0 };
  assert.equal(motivoIndisponibilidade(aberta, '2026-11-11'), 'Vagas esgotadas');
  assert.equal(motivoIndisponibilidade({ ...aberta, vagasDisponiveis: 1 }, '2026-11-11'), '');
  assert.equal(motivoIndisponibilidade({ ...aberta, data: '2020-01-01' }, '2026-11-11'), 'Oportunidade já realizada');
  assert.equal(motivoIndisponibilidade({ ...aberta, status: 'ENCERRADA' }, '2026-11-11'), 'Inscrições encerradas');
});
