import { useState } from 'react'
import { useAuth } from '../../context/AuthContext'
import { useNavigate, Link } from 'react-router-dom'

function Login() {
  const [email, setEmail] = useState('')
  const [senha, setSenha] = useState('')
  const { login } = useAuth()
  const navigate = useNavigate()

  const handleSubmit = (e) => {
    e.preventDefault()
    login(email, senha)
    navigate('/perfil')
  }

  return (
    <div style={{ maxWidth: '400px', margin: '2rem auto', padding: '2rem' }}>
      <h2>Entrar</h2>
      <form onSubmit={handleSubmit}>
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
          style={{ width: '100%', padding: '0.7rem', backgroundColor: '#4CAF50', color: 'white', border: 'none', borderRadius: '4px', fontSize: '1rem' }}
        >
          Entrar
        </button>
      </form>
      <p style={{ marginTop: '1rem', textAlign: 'center' }}>
        Não tem conta? <Link to="/cadastro">Cadastre-se</Link>
      </p>
    </div>
  )
}

export default Login