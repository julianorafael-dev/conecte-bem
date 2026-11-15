import test from 'node:test';
import assert from 'node:assert/strict';
import { hojeNoBrasil, limiteNascimento } from '../src/services/idade.js';

test('limite de 18 anos e anos bissextos', () => {
  assert.equal(limiteNascimento('2026-11-10'), '2008-11-10');
  assert.equal(limiteNascimento('2024-02-29'), '2006-02-28');
  assert.equal(limiteNascimento('2026-02-28'), '2008-02-28');
  assert.equal(limiteNascimento('2026-03-01'), '2008-03-01');
});

test('data usa São Paulo mesmo antes da meia-noite local', () => {
  assert.equal(hojeNoBrasil(new Date('2026-11-11T01:00:00Z')), '2026-11-10');
});
