import vagas from '../../mocks/vagasMock.json';

function Oportunidades() {
  return (
    <div style={{ padding: '2rem', maxWidth: '800px', margin: '0 auto' }}>
      <h1>Oportunidades de Voluntariado</h1>
      <p>Confira todas as vagas e causas disponíveis.</p>
      
      <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', marginTop: '2rem' }}>
        {vagas.map((vaga) => (
          <div key={vaga.id} style={{ border: '1px solid #ddd', padding: '1.5rem', borderRadius: '8px', backgroundColor: '#f9f9f9' }}>
            <h2 style={{ margin: '0 0 0.5rem 0', color: '#333' }}>{vaga.titulo}</h2>
            <p style={{ margin: '0 0 1rem 0', color: '#666' }}>
              <strong>{vaga.ong.nome}</strong> • {vaga.categoria} • {vaga.localizacao}
            </p>
            <p>{vaga.descricao}</p>
            
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '1rem' }}>
              <span style={{ fontSize: '0.9rem', color: vaga.vagasDisponiveis > 0 ? 'green' : 'red' }}>
                {vaga.vagasDisponiveis} vagas disponíveis de {vaga.vagasTotais}
              </span>
              <button style={{ padding: '0.5rem 1rem', cursor: 'pointer', backgroundColor: '#0056b3', color: 'white', border: 'none', borderRadius: '4px' }}>
                Ver Detalhes
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

export default Oportunidades;