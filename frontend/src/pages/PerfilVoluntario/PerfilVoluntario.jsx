import './PerfilVoluntario.css'

function PerfilVoluntario() {
  return (
    <div className="perfil-voluntario-container">
      <h1>Perfil do Voluntário</h1>
            <form className="editar-dados" action="#">
                <h2>Dados:</h2>
                <div className="form-grupo">
                    <label htmlFor="nome">Nome do Voluntário:</label>
                    <input className="form-control" type="text" id="nome" name="nome" value="Voluntário Exemplo" />
                </div>
                <div className="form-grupo">
                    <label htmlFor="email">E-mail:</label>
                    <input className="form-control" type="email" id="email" name="email" placeholder="exemplo@ong.com"/>
                </div>
                <div className="form-grupo">
                    <h3>CPF:</h3>
                    <p id="cpf"></p>
                </div>
                <div className="form-grupo">
                    <label htmlFor="cpf">CPF:</label>
                    <input className="form-control" type="text" id="cpf" name="cpf" value="123.456.789-00" disabled />
                </div>
                <div className="form-grupo">
                    <label htmlFor="senha">Nova senha:</label>
                    <input className="form-control" type="password" id="senha" name="senha" placeholder="Digite a nova senha" />
                </div>
                <div className="form-grupo">
                    <label htmlFor="confirmarSenha">Confirmar nova senha:</label>
                    <input className="form-control" type="password" id="confirmarSenha" name="confirmarSenha" placeholder="Confirme a nova senha" />
                </div>
                
                <button className="btn btn-primary" type="submit">Salvar alterações</button>
            </form>
    </div>
  );
}

export default PerfilVoluntario;