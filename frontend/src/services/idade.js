export function hojeNoBrasil(instante = new Date()) {
  const partes = new Intl.DateTimeFormat('en-CA', {
    timeZone: 'America/Sao_Paulo', year: 'numeric', month: '2-digit', day: '2-digit',
  }).formatToParts(instante);
  const valor = (tipo) => partes.find(parte => parte.type === tipo).value;
  return `${valor('year')}-${valor('month')}-${valor('day')}`;
}

export function limiteNascimento(hoje = hojeNoBrasil()) {
  const [ano, mes, dia] = hoje.split('-').map(Number);
  const ultimoDia = new Date(Date.UTC(ano - 18, mes, 0)).getUTCDate();
  return `${ano - 18}-${String(mes).padStart(2, '0')}-${String(Math.min(dia, ultimoDia)).padStart(2, '0')}`;
}
