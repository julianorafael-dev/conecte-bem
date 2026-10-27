import { useState } from 'react'
import { useAuth } from '../context/AuthContext'
import { useNavigate, Link } from 'react-router-dom'

function Login() {
  const [tipo, setTipo] = useState('voluntario')
  const [email, setEmail] = useState('')
  const [senha, setSenha] = useState('')
  const { login } = useAuth()
  const navigate = useNavigate()

  const [modalAberto, setModalAberto] = useState(false)
  const [emailRecuperacao, setEmailRecuperacao] = useState('')
  const [dataNascimento, setDataNascimento] = useState('')

  const handleSubmit = (e) => {
    e.preventDefault()
    login(email, senha, tipo)
    navigate('/home')
  }

  const handleRecuperarSenha = (e) => {
    e.preventDefault()
    alert(`E-mail: ${emailRecuperacao}\nData de nascimento: ${dataNascimento}`)
    setModalAberto(false)
  }

  return (
    <div style={{ maxWidth: '420px', margin: '3rem auto', padding: '2rem' }}>
      <h2 style={{ textAlign: 'center', marginBottom: '2rem' }}>Acessar</h2>

      {/* Toggle Voluntário / ONG */}
      <div style={{ display: 'flex', marginBottom: '2rem', borderRadius: '8px', border: '1px solid #ddd', overflow: 'hidden' }}>
        <button
          type="button"
          onClick={() => setTipo('voluntario')}
          style={{
            flex: 1, padding: '0.8rem', border: 'none',
            backgroundColor: tipo === 'voluntario' ? '#4CAF50' : 'transparent',
            color: tipo === 'voluntario' ? 'white' : '#666',
            cursor: 'pointer', fontWeight: tipo === 'voluntario' ? 'bold' : 'normal'
          }}
        >
          Voluntário
        </button>
        <button
          type="button"
          onClick={() => setTipo('ong')}
          style={{
            flex: 1, padding: '0.8rem', border: 'none',
            backgroundColor: tipo === 'ong' ? '#4CAF50' : 'transparent',
            color: tipo === 'ong' ? 'white' : '#666',
            cursor: 'pointer', fontWeight: tipo === 'ong' ? 'bold' : 'normal'
          }}
        >
          ONG
        </button>
      </div>

      <form onSubmit={handleSubmit}>
        <div style={{ marginBottom: '1rem' }}>
          <label style={{ display: 'block', marginBottom: '0.3rem' }}>E-mail</label>
          <input
            type="email" value={email}
            onChange={(e) => setEmail(e.target.value)} required
            style={{ width: '100%', padding: '0.7rem', border: '1px solid #ddd', borderRadius: '4px' }}
          />
        </div>

        <div style={{ marginBottom: '1rem' }}>
          <label style={{ display: 'block', marginBottom: '0.3rem' }}>Senha</label>
          <input
            type="password" value={senha}
            onChange={(e) => setSenha(e.target.value)} required
            style={{ width: '100%', padding: '0.7rem', border: '1px solid #ddd', borderRadius: '4px' }}
          />
        </div>

        <button
          type="submit"
          style={{
            width: '100%', padding: '0.9rem', backgroundColor: '#4CAF50',
            color: 'white', border: 'none', borderRadius: '4px',
            fontSize: '1rem', cursor: 'pointer', marginTop: '0.5rem'
          }}
        >
          Acessar
        </button>
      </form>

      <p style={{ textAlign: 'center', marginTop: '1rem' }}>
        <button
          type="button"
          onClick={() => setModalAberto(true)}
          style={{
            background: 'none', border: 'none', color: '#0066cc',
            textDecoration: 'underline', cursor: 'pointer', fontSize: '0.9rem'
          }}
        >
          Esqueci minha senha
        </button>
      </p>

      <p style={{ textAlign: 'center', marginTop: '1.5rem' }}>
        Não tem conta? <Link to="/criar-conta" style={{ color: '#4CAF50', fontWeight: 'bold' }}>Cadastre-se</Link>
      </p>

      {/* Modal Recuperar Senha */}
      {modalAberto && (
        <div style={{
          position: 'fixed', top: 0, left: 0, width: '100%', height: '100%',
          backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000
        }}>
          <div style={{ backgroundColor: 'white', padding: '2rem', borderRadius: '8px', width: '90%', maxWidth: '400px' }}>
            <h3 style={{ marginTop: 0, marginBottom: '1.5rem' }}>Recuperar Senha</h3>
            <form onSubmit={handleRecuperarSenha}>
              <div style={{ marginBottom: '1rem' }}>
                <label style={{ display: 'block', marginBottom: '0.3rem' }}>E-mail</label>
                <input
                  type="email" value={emailRecuperacao}
                  onChange={(e) => setEmailRecuperacao(e.target.value)} required
                  style={{ width: '100%', padding: '0.7rem', border: '1px solid #ddd', borderRadius: '4px' }}
                />
              </div>
              <div style={{ marginBottom: '1.5rem' }}>
                <label style={{ display: 'block', marginBottom: '0.3rem' }}>Data de Nascimento</label>
                <input
                  type="date" value={dataNascimento}
                  onChange={(e) => setDataNascimento(e.target.value)} required
                  style={{ width: '100%', padding: '0.7rem', border: '1px solid #ddd', borderRadius: '4px' }}
                />
              </div>
              <div style={{ display: 'flex', gap: '0.8rem' }}>
                <button
                  type="button" onClick={() => setModalAberto(false)}
                  style={{ flex: 1, padding: '0.7rem', backgroundColor: '#eee', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  style={{ flex: 1, padding: '0.7rem', backgroundColor: '#4CAF50', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
                >
                  Continuar
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}

export default Login