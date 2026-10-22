import { useEffect, useState } from 'react';
import { api } from '../../services/api.js';
import { getUsuario, isOng } from '../../services/auth.js';
import './PerfilOng.css';

const estados = 'AC AL AP AM BA CE DF ES GO MA MT MS MG PA PB PR PE PI RJ RN RS RO RR SC SP SE TO'.split(' ');

function PerfilOng() {
    const [dados, setDados] = useState(null);
    const [salvos, setSalvos] = useState(null);
    const [carregando, setCarregando] = useState(true);
    const [salvando, setSalvando] = useState(false);
    const [erro, setErro] = useState('');
    const [mensagem, setMensagem] = useState('');

    useEffect(() => {
        let ativo = true;
        async function carregar() {
            try {
                if (!isOng()) throw new Error('Entre com uma conta de ONG para editar seu perfil.');
                const ong = await api('/ongs/me');
                const perfil = Object.fromEntries(Object.entries(ong).map(([chave, valor]) => [chave, valor ?? '']));
                if (ativo) {
                    setDados(perfil);
                    setSalvos(perfil);
                }
            } catch (error) {
                if (ativo) setErro(error.message);
            } finally {
                if (ativo) setCarregando(false);
            }
        }
        carregar();
        return () => { ativo = false; };
    }, []);

    function alterar(event) {
        const { name, value } = event.target;
        setDados(anterior => ({ ...anterior, [name]: value }));
        setMensagem('');
        setErro('');
    }

    async function salvar(event) {
        event.preventDefault();
        setSalvando(true);
        setErro('');
        setMensagem('');
        try {
            const perfil = await api('/ongs/me', {
                method: 'PUT',
                body: {
                    nome: dados.nome.trim(),
                    descricao: dados.descricao.trim(),
                    telefone: dados.telefone.trim(),
                    cidade: dados.cidade.trim(),
                    estado: dados.estado || null,
                },
            });
            const atualizado = Object.fromEntries(Object.entries(perfil).map(([chave, valor]) => [chave, valor ?? '']));
            setDados(atualizado);
            setSalvos(atualizado);
            setMensagem('Alterações salvas com sucesso.');
        } catch (error) {
            setErro(error.message);
        } finally {
            setSalvando(false);
        }
    }

    return (
        <main className="perfil-ong-container">
            <h1>Perfil da ONG</h1>
            <p>Atualize as informações da sua organização.</p>
            {carregando && <p role="status">Carregando perfil...</p>}
            {erro && <p className="perfil-ong-erro" role="alert">{erro}</p>}
            {mensagem && <p className="perfil-ong-sucesso" role="status">{mensagem}</p>}
            {dados && (
                <form className="editar-dados" onSubmit={salvar}>
                    <fieldset disabled={salvando}>
                        <legend>Dados da organização</legend>
                        <div className="form-grupo">
                            <label htmlFor="nome">Nome da ONG</label>
                            <input className="form-control" id="nome" name="nome" value={dados.nome} onChange={alterar} required maxLength={150} pattern=".*\S.*" autoComplete="organization" />
                        </div>
                        <div className="form-grupo">
                            <label htmlFor="email">E-mail da conta</label>
                            <input className="form-control" id="email" type="email" value={getUsuario()?.email || ''} readOnly />
                        </div>
                        <div className="form-grupo">
                            <label htmlFor="cnpj">CNPJ</label>
                            <input className="form-control" id="cnpj" value={dados.cnpj} readOnly />
                        </div>
                        <div className="form-grupo">
                            <label htmlFor="descricao">Descrição da ONG</label>
                            <textarea className="form-control" id="descricao" name="descricao" value={dados.descricao} onChange={alterar} placeholder="Conte sobre a atuação da ONG" />
                        </div>
                        <div className="form-grupo">
                            <label htmlFor="telefone">Telefone</label>
                            <input className="form-control" id="telefone" name="telefone" type="tel" maxLength={20} autoComplete="tel" value={dados.telefone} onChange={alterar} />
                        </div>
                    </fieldset>
                    <fieldset disabled={salvando}>
                        <legend>Localização</legend>
                        <div className="form-grupo">
                            <label htmlFor="cidade">Cidade</label>
                            <input className="form-control" id="cidade" name="cidade" maxLength={100} autoComplete="address-level2" value={dados.cidade} onChange={alterar} />
                        </div>
                        <div className="form-grupo">
                            <label htmlFor="estado">Estado</label>
                            <select className="form-control" id="estado" name="estado" autoComplete="address-level1" value={dados.estado} onChange={alterar}>
                                <option value="">Selecione um estado</option>
                                {estados.map(estado => <option key={estado} value={estado}>{estado}</option>)}
                            </select>
                        </div>
                    </fieldset>
                    <div className="perfil-ong-acoes">
                        <button className="btn" type="button" disabled={salvando} onClick={() => { setDados(salvos); setErro(''); setMensagem(''); }}>Cancelar alterações</button>
                        <button className="btn btn-primary" type="submit" disabled={salvando}>{salvando ? 'Salvando...' : 'Salvar alterações'}</button>
                    </div>
                </form>
            )}
        </main>
    );
}

export default PerfilOng;
