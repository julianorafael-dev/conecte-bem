import { useState } from 'react'
import { useAuth } from '../../hooks/useAuth.js'
import { useNavigate, useLocation, Link, Navigate } from 'react-router-dom'

function destinoPorTipo(tipo) {
  return tipo === 'ONG' ? '/painel' : '/Home'
}

function Login() {
  const [email, setEmail] = useState('')
  const [senha, setSenha] = useState('')
  const [erro, setErro] = useState('')
  const [enviando, setEnviando] = useState(false)
  const { usuario, login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const [modalAberto, setModalAberto] = useState(false)
  const [emailRecuperacao, setEmailRecuperacao] = useState('')
  const [dataNascimento, setDataNascimento] = useState('')

  // Quem já está logado não precisa ver a tela de login.
  if (usuario && !enviando) {
    return <Navigate to={destinoPorTipo(usuario.tipo)} replace />
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setErro('')
    setEnviando(true)
    try {
      const autenticado = await login(email.trim(), senha)
      navigate(destinoPorTipo(autenticado?.tipo), { replace: true })
    } catch (error) {
      setErro(error.message)
      setEnviando(false)
    }
  }

  const handleRecuperarSenha = (e) => {
    e.preventDefault()
    alert(`E-mail: ${emailRecuperacao}\nData de nascimento: ${dataNascimento}`)
    setModalAberto(false)
  }

  return (
    <div style={{ maxWidth: '420px', margin: '3rem auto', padding: '2rem' }}>
      <h2 style={{ textAlign: 'center', marginBottom: '2rem' }}>Acessar</h2>

      {location.state?.mensagem && (
        <p role="status" style={{ color: '#2e7d32', textAlign: 'center', marginBottom: '1rem' }}>
          {location.state.mensagem}
        </p>
      )}

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

        {erro && (
          <p role="alert" style={{ color: '#c62828', marginBottom: '1rem' }}>{erro}</p>
        )}

        <button
          type="submit"
          disabled={enviando}
          style={{
            width: '100%', padding: '0.9rem', backgroundColor: '#4CAF50',
            color: 'white', border: 'none', borderRadius: '4px',
            fontSize: '1rem', cursor: enviando ? 'wait' : 'pointer', marginTop: '0.5rem',
            opacity: enviando ? 0.7 : 1
          }}
        >
          {enviando ? 'Entrando...' : 'Acessar'}
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
        Não tem conta? <Link to="/CriarConta" style={{ color: '#4CAF50', fontWeight: 'bold' }}>Cadastre-se</Link>
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