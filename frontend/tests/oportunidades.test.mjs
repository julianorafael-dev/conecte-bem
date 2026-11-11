import test from 'node:test';
import assert from 'node:assert/strict';
import { motivoIndisponibilidade, candidaturaCancelada, pertenceAoFiltro } from '../src/services/oportunidades.js';

test('disponibilidade considera vagas restantes, data e status', () => {
  const aberta = { status: 'ABERTA', data: '2027-01-01', vagas: 5, vagasDisponiveis: 0 };
  assert.equal(motivoIndisponibilidade(aberta, '2026-11-11'), 'Vagas esgotadas');
  assert.equal(motivoIndisponibilidade({ ...aberta, vagasDisponiveis: 1 }, '2026-11-11'), '');
  assert.equal(motivoIndisponibilidade({ ...aberta, data: '2020-01-01' }, '2026-11-11'), 'Oportunidade já realizada');
  assert.equal(motivoIndisponibilidade({ ...aberta, status: 'ENCERRADA' }, '2026-11-11'), 'Inscrições encerradas');
});

test('cancelamento da oportunidade classifica candidatura ativa como cancelada', () => {
  const candidatura = { statusInscricao: 'CONFIRMADA', statusOportunidade: 'CANCELADA' };
  assert.equal(candidaturaCancelada(candidatura), true);
  assert.equal(pertenceAoFiltro(candidatura, 'canceladas'), true);
  assert.equal(pertenceAoFiltro(candidatura, 'todas'), true);
  assert.equal(pertenceAoFiltro(candidatura, 'ativas'), false);
  assert.equal(pertenceAoFiltro(candidatura, 'finalizadas'), false);
});

test('cancelamento individual não vira candidatura finalizada', () => {
  const candidatura = { statusInscricao: 'CANCELADA', statusOportunidade: 'ENCERRADA' };
  assert.equal(pertenceAoFiltro(candidatura, 'canceladas'), true);
  assert.equal(pertenceAoFiltro(candidatura, 'finalizadas'), false);
  assert.equal(pertenceAoFiltro({ ...candidatura, statusInscricao: 'PENDENTE' }, 'finalizadas'), true);
});
