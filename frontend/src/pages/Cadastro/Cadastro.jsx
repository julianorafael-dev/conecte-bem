import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'

function Cadastro() {
  const [nome, setNome] = useState('')
  const [email, setEmail] = useState('')
  const [senha, setSenha] = useState('')
  const navigate = useNavigate()

  const handleSubmit = (e) => {
    e.preventDefault()
    // Aqui você pode adicionar a lógica de salvar o usuário
    console.log('Cadastro:', { nome, email, senha })
    alert('Cadastro realizado! Faça login.')
    navigate('/')
  }

  return (
    <div style={{ maxWidth: '400px', margin: '2rem auto', padding: '2rem' }}>
      <h2>Cadastrar</h2>
      <form onSubmit={handleSubmit}>
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
          <label>Email:</label>
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
          style={{ width: '100%', padding: '0.7rem', backgroundColor: '#007bff', color: 'white', border: 'none', borderRadius: '4px', fontSize: '1rem' }}
        >
          Cadastrar
        </button>
      </form>
      <p style={{ marginTop: '1rem', textAlign: 'center' }}>
        Já tem conta? <Link to="/">Entrar</Link>
      </p>
    </div>
  )
}

export default Cadastro