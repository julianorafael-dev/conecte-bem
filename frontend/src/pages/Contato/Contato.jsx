import { useState } from 'react';
import { api } from '../../services/api.js';
import './Contato.css';

const VAZIO = { nome: '', email: '', mensagem: '' };

function Contato() {
  const [dados, setDados] = useState(VAZIO);
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState('');
  const [enviado, setEnviado] = useState(false);

  function alterar(event) {
    const { name, value } = event.target;
    setDados((anterior) => ({ ...anterior, [name]: value }));
    setErro('');
    setEnviado(false);
  }

  async function enviar(event) {
    event.preventDefault();
    if (enviando) return;
    setErro('');
    setEnviando(true);
    try {
      await api('/contato', { method: 'POST', body: dados });
      setDados(VAZIO);
      setEnviado(true);
    } catch (error) {
      setErro(error.message);
    } finally {
      setEnviando(false);
    }
  }

  return (
    <div className="contato-container">
      <div className="contato-header">
        <h1>Contato</h1>
        <p>Envie sua dúvida, crítica ou sugestão!</p>
      </div>

      {enviado && (
        <p className="contato-sucesso" role="status">
          Mensagem enviada com sucesso! Em breve entraremos em contato pelo e-mail informado.
        </p>
      )}
      {erro && <p className="contato-erro" role="alert">{erro}</p>}

      <form className="contato-form" onSubmit={enviar}>
        <fieldset disabled={enviando}>
          <div className="form-group">
            <label htmlFor="nome">Nome</label>
            <input
              type="text"
              id="nome"
              name="nome"
              placeholder="Preencha seu nome.."
              value={dados.nome}
              onChange={alterar}
              required
              maxLength={150}
              autoComplete="name"
            />
          </div>

          <div className="form-group">
            <label htmlFor="email">E-mail</label>
            <input
              type="email"
              id="email"
              name="email"
              placeholder="Preencha seu e-mail.."
              value={dados.email}
              onChange={alterar}
              required
              maxLength={150}
              autoComplete="email"
            />
          </div>

          <div className="form-group">
            <label htmlFor="mensagem">Mensagem</label>
            <textarea
              id="mensagem"
              name="mensagem"
              placeholder="Envie sua mensagem.."
              value={dados.mensagem}
              onChange={alterar}
              required
              maxLength={2000}
            />
          </div>

          <button type="submit" className="btn-submit">
            {enviando ? 'Enviando...' : 'Enviar'}
          </button>
        </fieldset>
      </form>
    </div>
  );
}

export default Contato;
