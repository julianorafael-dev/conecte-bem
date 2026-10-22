import { useEffect, useState } from 'react';
import { api } from '../../services/api.js';
import { getUsuario } from '../../services/auth.js';
import './PerfilVoluntario.css';

function PerfilVoluntario() {
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
                if (getUsuario()?.tipo !== 'VOLUNTARIO') {
                    throw new Error('Entre com uma conta de voluntário para editar seu perfil.');
                }
                const perfil = await api('/voluntarios/me');
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
        setDados(anterior => ({ ...anterior, nome: event.target.value }));
        setErro('');
        setMensagem('');
    }

    function cancelar() {
        setDados(salvos);
        setErro('');
        setMensagem('');
    }

    async function salvar(event) {
        event.preventDefault();
        if (salvando) return;
        setErro('');
        setMensagem('');
        const nome = dados.nome.trim();
        if (!nome) {
            setErro('Informe seu nome.');
            return;
        }
        setSalvando(true);
        try {
            const perfil = await api('/voluntarios/me', { method: 'PUT', body: { nome } });
            setDados(perfil);
            setSalvos(perfil);
            setMensagem('Alterações salvas com sucesso.');
        } catch (error) {
            setErro(error.message);
        } finally {
            setSalvando(false);
        }
    }

    return (
        <main className="perfil-voluntario-container">
            <h1>Perfil do Voluntário</h1>
            <p>Atualize suas informações pessoais.</p>
            {carregando && <p role="status">Carregando perfil...</p>}
            {erro && <p className="perfil-voluntario-erro" role="alert">{erro}</p>}
            {mensagem && <p className="perfil-voluntario-sucesso" role="status">{mensagem}</p>}
            {dados && (
                <form className="editar-dados" onSubmit={salvar}>
                    <fieldset disabled={salvando}>
                        <legend>Dados pessoais</legend>
                        <div className="form-grupo">
                            <label htmlFor="voluntario-nome">Nome completo</label>
                            <input className="form-control" id="voluntario-nome" name="nome" value={dados.nome} onChange={alterar} required maxLength={150} autoComplete="name" />
                        </div>
                        <div className="form-grupo">
                            <label htmlFor="voluntario-email">E-mail da conta</label>
                            <input className="form-control" id="voluntario-email" type="email" value={dados.email} readOnly autoComplete="email" aria-describedby="voluntario-email-ajuda" />
                            <small id="voluntario-email-ajuda">O e-mail da conta está disponível apenas para consulta.</small>
                        </div>
                    </fieldset>
                    <div className="perfil-voluntario-acoes">
                        <button className="btn" type="button" disabled={salvando} onClick={cancelar}>Cancelar alterações</button>
                        <button className="btn btn-primary" type="submit" disabled={salvando}>{salvando ? 'Salvando...' : 'Salvar alterações'}</button>
                    </div>
                </form>
            )}
        </main>
    );
}

export default PerfilVoluntario;
