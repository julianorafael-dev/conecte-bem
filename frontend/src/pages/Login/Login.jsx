import { useEffect, useRef, useState } from 'react'
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
  const [recuperacaoEnviada, setRecuperacaoEnviada] = useState(false)
  const botaoRecuperarRef = useRef(null)
  const dialogoRecuperacaoRef = useRef(null)
  const emailRecuperacaoRef = useRef(null)
  const fecharRecuperacaoRef = useRef(null)

  // Correção QA-019: Abre o modal automaticamente se vier do Footer com state { recuperar: true }
  useEffect(() => {
    if (location.state?.recuperar) {
      setModalAberto(true)
    }
  }, [location.state])

  function fecharModalRecuperacao() {
    setModalAberto(false)
    setRecuperacaoEnviada(false)
    setEmailRecuperacao('')
    setDataNascimento('')
    window.setTimeout(() => botaoRecuperarRef.current?.focus(), 0)
  }

  useEffect(() => {
    if (!modalAberto) return undefined

    // O foco começa no primeiro campo do diálogo e fica contido nele até o fechamento.
    window.setTimeout(() => emailRecuperacaoRef.current?.focus(), 0)

    function tratarTecla(evento) {
      if (evento.key === 'Escape') {
        evento.preventDefault()
        fecharModalRecuperacao()
        return
      }

      if (evento.key !== 'Tab') return

      const elementosFocaveis = dialogoRecuperacaoRef.current?.querySelectorAll(
          'button:not([disabled]), input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [href]'
      )
      if (!elementosFocaveis?.length) return

      const primeiro = elementosFocaveis[0]
      const ultimo = elementosFocaveis[elementosFocaveis.length - 1]

      if (evento.shiftKey && document.activeElement === primeiro) {
        evento.preventDefault()
        ultimo.focus()
      } else if (!evento.shiftKey && document.activeElement === ultimo) {
        evento.preventDefault()
        primeiro.focus()
      }
    }

    document.addEventListener('keydown', tratarTecla)
    return () => document.removeEventListener('keydown', tratarTecla)
  }, [modalAberto])

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
    setRecuperacaoEnviada(true)
    window.setTimeout(() => fecharRecuperacaoRef.current?.focus(), 0)
  }

  return (
      <div style={{ maxWidth: '420px', margin: '3rem auto', padding: '2rem' }}>
        {/* Correção QA-023: Título principal como H1 */}
        <h1 style={{ textAlign: 'center', marginBottom: '2rem', fontSize: '1.5em' }}>Acessar</h1>

        {location.state?.mensagem && (
            <p role="status" style={{ color: '#2e7d32', textAlign: 'center', marginBottom: '1rem' }}>
              {location.state.mensagem}
            </p>
        )}

        <form onSubmit={handleSubmit}>
          <div style={{ marginBottom: '1rem' }}>
            <label htmlFor="login-email" style={{ display: 'block', marginBottom: '0.3rem' }}>E-mail</label>
            <input
                id="login-email" name="email" type="email" autoComplete="email" value={email}
                onChange={(e) => setEmail(e.target.value)} required
                style={{ width: '100%', padding: '0.7rem', border: '1px solid #ddd', borderRadius: '4px' }}
            />
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label htmlFor="login-senha" style={{ display: 'block', marginBottom: '0.3rem' }}>Senha</label>
            <input
                id="login-senha" name="password" type="password" autoComplete="current-password" value={senha}
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
                width: '100%', padding: '0.9rem', backgroundColor: '#2e7d32',
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
              ref={botaoRecuperarRef}
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
          Não tem conta? <Link to="/CriarConta" style={{ color: '#2e7d32', fontWeight: 'bold' }}>Cadastre-se</Link>
        </p>

        {/* Modal Recuperar Senha */}
        {modalAberto && (
            <div style={{
              position: 'fixed', top: 0, left: 0, width: '100%', height: '100%',
              backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000
            }}>
              <div ref={dialogoRecuperacaoRef} role="dialog" aria-modal="true" aria-labelledby="titulo-recuperar-senha" style={{ backgroundColor: 'white', padding: '2rem', borderRadius: '8px', width: '90%', maxWidth: '400px' }}>
                <h3 id="titulo-recuperar-senha" style={{ marginTop: 0, marginBottom: '1.5rem' }}>Recuperar Senha</h3>
                {recuperacaoEnviada ? (
                    <>
                      <p role="status" style={{ color: '#2e7d32', marginBottom: '1.5rem' }}>
                        Enviamos um link de recuperação para o e-mail informado.
                      </p>
                      <button
                          ref={fecharRecuperacaoRef}
                          type="button" onClick={fecharModalRecuperacao}
                          style={{ width: '100%', padding: '0.7rem', backgroundColor: '#2e7d32', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
                      >
                        Fechar
                      </button>
                    </>
                ) : (
                    <form onSubmit={handleRecuperarSenha}>
                      <div style={{ marginBottom: '1rem' }}>
                        <label htmlFor="recuperar-email" style={{ display: 'block', marginBottom: '0.3rem' }}>E-mail</label>
                        <input
                            ref={emailRecuperacaoRef} id="recuperar-email" name="recuperar-email" type="email" autoComplete="email" value={emailRecuperacao}
                            onChange={(e) => setEmailRecuperacao(e.target.value)} required
                            style={{ width: '100%', padding: '0.7rem', border: '1px solid #ddd', borderRadius: '4px' }}
                        />
                      </div>
                      <div style={{ marginBottom: '1.5rem' }}>
                        <label htmlFor="recuperar-data-nascimento" style={{ display: 'block', marginBottom: '0.3rem' }}>Data de Nascimento</label>
                        <input
                            id="recuperar-data-nascimento" name="recuperar-data-nascimento" type="date" autoComplete="bday" value={dataNascimento}
                            onChange={(e) => setDataNascimento(e.target.value)} required
                            style={{ width: '100%', padding: '0.7rem', border: '1px solid #ddd', borderRadius: '4px' }}
                        />
                      </div>
                      <div style={{ display: 'flex', gap: '0.8rem' }}>
                        <button
                            type="button" onClick={fecharModalRecuperacao}
                            style={{ flex: 1, padding: '0.7rem', backgroundColor: '#eee', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
                        >
                          Cancelar
                        </button>
                        <button
                            type="submit"
                            style={{ flex: 1, padding: '0.7rem', backgroundColor: '#2e7d32', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
                        >
                          Continuar
                        </button>
                      </div>
                    </form>
                )}
              </div>
            </div>
        )}
      </div>
  )
}

export default Login