import "./PerfilOng.css";



function PerfilOng() {
    return (
        <main className="perfil-ong-container">
            <h1>Perfil da ONG</h1>
            <form className="editar-dados" action="#">
                <h2>Dados:</h2>
                <div className="form-grupo">
                    <label htmlFor="nome">Nome da ONG:</label>
                    <input className="form-control" type="text" id="nome" name="nome" value="ONG Exemplo" disabled />
                </div>
                <div className="form-grupo">
                    <label htmlFor="email">E-mail:</label>
                    <input className="form-control" type="email" id="email" name="email" value="exemplo@ong.com" disabled />
                </div>
                <div className="form-grupo">
                    <h3>CNPJ:</h3>
                    <p id="cnpj"></p>
                </div>
                <div className="form-grupo">
                    <label htmlFor="senha">Nova senha:</label>
                    <input className="form-control" type="password" id="senha" name="senha" placeholder="Digite a nova senha" />
                </div>
                <div className="form-grupo">
                    <label htmlFor="confirmarSenha">Confirmar nova senha:</label>
                    <input className="form-control" type="password" id="confirmarSenha" name="confirmarSenha" placeholder="Confirme a nova senha" />
                </div>
                <div className="form-grupo">
                    <label htmlFor="descricao">Descrição da ONG:</label>
                    <textarea className="form-control" id="descricao" name="descricao" placeholder="Digite uma breve descrição da ONG"></textarea>
                </div>
                <div className="form-grupo">
                    <label htmlFor="telefone">Telefone:</label>
                    <input className="form-control" type="tel" id="telefone" name="telefone" placeholder="Digite o telefone da ONG" />
                </div>
                <h3>Endereço:</h3>
                <div className="form-grupo">
                    <label htmlFor="cidade">Cidade:</label>
                    <input className="form-control" type="text" id="cidade" name="cidade" placeholder="Digite a cidade da ONG" />
                </div>
                <div className="form-grupo">
                    <label htmlFor="estado">Estado:</label>
                    <input className="form-control" type="text" id="estado" name="estado" placeholder="Digite o estado da ONG" />
                </div>
                <button className="btn btn-primary" type="submit">Salvar alterações</button>
            </form>
        </main>
    );
}
export default PerfilOng;