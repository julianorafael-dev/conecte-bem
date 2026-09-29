import './Contato.css';

function Contato() {
  return (
    <div className="contato-container">
      <div className="contato-header">
        <h1>Contato</h1>
        <p>Envie sua dúvida, crítica ou sugestão!</p>
      </div>

      <form className="contato-form">
        <div className="form-group">
          <label htmlFor="nome">Nome</label>
          <input 
            type="text" 
            id="nome" 
            name="nome" 
            placeholder="Preencha seu nome.." 
          />
        </div>

        <div className="form-group">
          <label htmlFor="email">E-mail</label>
          <input 
            type="email" 
            id="email" 
            name="email" 
            placeholder="Preencha seu e-mail.." 
          />
        </div>

        <div className="form-group">
          <label htmlFor="assunto">Assunto</label>
          <textarea 
            id="assunto" 
            name="assunto" 
            placeholder="Envie sua mensagem.." 
          />
        </div>

        <button type="submit" className="btn-submit">
          Enviar
        </button>
      </form>
    </div>
  );
}

export default Contato;