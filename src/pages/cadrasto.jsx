import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

function Cadastro() {
  const [nome, setNome] = useState('');
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const navigate = useNavigate();

  const handleCadastro = (e) => {
    e.preventDefault();
    // Aqui vai conectar com a API
    alert('Cadastro realizado! Faça login.');
    navigate('/');
  };

  return (
    <div style={{ maxWidth: '400px', margin: '50px auto', padding: '2rem', border: '1px solid #ccc', borderRadius: '8px' }}>
      <h2>Cadastrar</h2>
      <form onSubmit={handleCadastro}>
        <div style={{ marginBottom: '1rem' }}>
          <label>Nome:</label>
          <input
            type="text"
            value={nome}
            onChange={(e) => setNome(e.target.value)}
            required
            style={{ width: '100%', padding: '0.5rem', marginTop: '0.3rem' }}
          />
        </div>
        <div style={{ marginBottom: '1rem' }}>
          <label>E-mail:</label>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            style={{ width: '100%', padding: '0.5rem', marginTop: '0.3rem' }}
          />
        </div>
        <div style={{ marginBottom: '1rem' }}>
          <label>Senha:</label>
          <input
            type="password"
            value={senha}
            onChange={(e) => setSenha(e.target.value)}
            required
            style={{ width: '100%', padding: '0.5rem', marginTop: '0.3rem' }}
          />
        </div>
        <button
          type="submit"
          style={{ width: '100%', padding: '0.7rem', background: '#333', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
        >
          Cadastrar
        </button>
      </form>
    </div>
  );
}

export default Cadastro;