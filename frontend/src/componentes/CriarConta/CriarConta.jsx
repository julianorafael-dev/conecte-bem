import { useState } from 'react';
import './CriarConta.css';

function CriarConta() {
    // 1. Estados do Formulário
    const [formData, setFormData] = useState({
        email: '',
        tipoPessoa: 'CPF',
        nomeCompleto: '',
        diaNascimento: '',
        mesNascimento: '',
        anoNascimento: '',
        cpfCnpj: '',
        telefone: '',
        senha: ''
    });

    // Estado para controlar a visibilidade da senha
    const [mostrarSenha, setMostrarSenha] = useState(false);

    // 2. Função para alternar a exibição da senha
    const toggleMostrarSenha = () => {
        setMostrarSenha(!mostrarSenha);
    };

    // 3. Função para restringir entradas (Apenas números)
    const handleApenasNumerosChange = (e) => {
        const { name, value } = e.target;
        // Remove tudo que não for número (0-9)
        const valorApenasNumeros = value.replace(/\D/g, '');

        setFormData(prev => ({
            ...prev,
            [name]: valorApenasNumeros
        }));
    };

    // 4. Função para campos de texto normais
    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    // 5. Envio do Formulário
    const handleSubmit = (e) => {
        e.preventDefault();
        
        // Exemplo de ação ao enviar o formulário
        alert('Cadastro realizado com sucesso!');
        console.log('Dados do cadastro:', formData);
    };

    return (
        <div className="criar-conta-container">
            <h1>Preencha o formulário</h1>

            {/* Envolvemos os campos dentro de uma tag <form> */}
            <form className="form" onSubmit={handleSubmit}>
                
                {/* E-mail */}
                <div className="form-grupo">
                    <label htmlFor="e-mail">E-mail</label>
                    <input 
                        id="e-mail" 
                        name="email" 
                        type="email" 
                        className="form-control" 
                        required 
                        placeholder="Ex: usuario@mail.com"
                        value={formData.email}
                        onChange={handleChange}
                    />
                </div>

                {/* Tipo de Pessoa */}
                <div className="form-grupo-radio">
                    <div>
                        <input 
                            name="tipoPessoa" 
                            type="radio" 
                            id="tipoCpf" 
                            value="CPF" 
                            checked={formData.tipoPessoa === 'CPF'}
                            onChange={handleChange}
                        />
                        <label htmlFor="tipoCpf" className="form-check-label">Pessoa Física</label>
                    </div>
                    <div>
                        <input 
                            name="tipoPessoa" 
                            type="radio" 
                            id="tipoCnpj" 
                            value="CNPJ" 
                            checked={formData.tipoPessoa === 'CNPJ'}
                            onChange={handleChange}
                        />
                        <label htmlFor="tipoCnpj" className="form-check-label">Pessoa Jurídica</label>
                    </div>
                </div>

                {/* Nome Completo */}
                <div className="form-grupo">
                    <label htmlFor="nome-completo">Nome completo</label>
                    <input 
                        id="nome-completo" 
                        name="nomeCompleto" 
                        type="text" 
                        className="form-control" 
                        required 
                        placeholder="João Silva" 
                        minLength="4" 
                        value={formData.nomeCompleto}
                        onChange={handleChange}
                    />
                </div>

                {/* Data de Nascimento - Dia */}
                <div className="form-grupo-nascimento">
                    <label htmlFor="data-de-nascimento">Data de nascimento</label>
                    <input 
                        id="data-de-nascimento" 
                        name="diaNascimento" 
                        type="text" 
                        inputMode="numeric"
                        className="form-control" 
                        required 
                        placeholder="DD" 
                        maxLength="2" 
                        value={formData.diaNascimento}
                        onChange={handleApenasNumerosChange}
                    />
                </div>

                {/* Data de Nascimento - Mês */}
                <div className="form-grupo-nascimento">
                    <label htmlFor="mes-de-nascimento">Mês de nascimento</label>
                    <input 
                        id="mes-de-nascimento" 
                        name="mesNascimento" 
                        type="text" 
                        inputMode="numeric"
                        className="form-control" 
                        required 
                        placeholder="MM" 
                        maxLength="2" 
                        value={formData.mesNascimento}
                        onChange={handleApenasNumerosChange}
                    />
                </div>

                {/* Data de Nascimento - Ano */}
                <div className="form-grupo-nascimento">
                    <label htmlFor="ano-de-nascimento">Ano de nascimento</label>
                    <input 
                        id="ano-de-nascimento" 
                        name="anoNascimento" 
                        type="text" 
                        inputMode="numeric"
                        className="form-control" 
                        required 
                        placeholder="AAAA" 
                        maxLength="4" 
                        value={formData.anoNascimento}
                        onChange={handleApenasNumerosChange}
                    />
                </div>

                <div className="nascimento-texto">É preciso ter 18 anos ou mais para criar uma conta.</div>

                {/* CPF / CNPJ - Apenas Números */}
                <div className="form-grupo">
                    <label htmlFor="cpf">{formData.tipoPessoa === 'CPF' ? 'CPF' : 'CNPJ'}</label>
                    <input 
                        id="cpf" 
                        name="cpfCnpj" 
                        type="text" 
                        inputMode="numeric"
                        className="form-control" 
                        maxLength={formData.tipoPessoa === 'CPF' ? 11 : 14} 
                        placeholder={formData.tipoPessoa === 'CPF' ? "00000000000" : "00000000000000"} 
                        value={formData.cpfCnpj}
                        onChange={handleApenasNumerosChange}
                    />
                </div>

                {/* Telefone - Apenas Números */}
                <div className="form-grupo">
                    <label htmlFor="telefone">Telefone</label>
                    <input 
                        id="telefone" 
                        name="telefone" 
                        type="text" 
                        inputMode="numeric"
                        className="form-control" 
                        maxLength="11"
                        placeholder="00900000000" 
                        value={formData.telefone}
                        onChange={handleApenasNumerosChange}
                    />
                </div>

                {/* Senha com alternância de visibilidade */}
                <div className="form-grupo">
                    <label htmlFor="senha">Senha</label>
                    <div className="form-control-password form-control">
                        <input 
                            id="senha" 
                            name="senha" 
                            type={mostrarSenha ? "text" : "password"} 
                            className="form-control form-control-inner-password" 
                            required 
                            minLength="6" 
                            placeholder="Deve conter pelo menos 6 caracteres" 
                            value={formData.senha}
                            onChange={handleChange}
                        />
                        <button 
                            type="button" 
                            className="toggle-show-password" 
                            onClick={toggleMostrarSenha}
                            title={mostrarSenha ? "Ocultar senha" : "Mostrar senha"}
                        >
                            <svg stroke="currentColor" fill="currentColor" strokeWidth="0" viewBox="0 0 512 512" height="20" width="20" xmlns="http://www.w3.org/2000/svg">
                                <path d="M88.3 68.1c-5.6-5.5-14.6-5.5-20.1.1-5.5 5.5-5.5 14.5 0 20l355.5 355.7c3.7 3.7 9 4.9 13.7 3.6 2.4-.6 4.6-1.9 6.4-3.7 5.5-5.5 5.5-14.5 0-20L88.3 68.1zM260.2 345.9c-53 2.4-96.6-41.2-94.1-94.1.6-12.2 3.6-23.8 8.6-34.3L121.3 164c-27.7 21.4-55.4 48.9-85.1 81.3-5.5 6.1-5.6 15.2-.1 21.3C101 338.3 158.2 400 255.8 400c29.7 0 57.1-7.4 82.3-19.2l-43.5-43.5c-10.6 5-22.2 8-34.4 8.6zM475.8 266c5.3-5.8 5.6-14.6.5-20.7C424 181.8 351.5 112 255.8 112c-29.1 0-56 6.6-82 19l43.7 43.7c10.5-5 22.1-8.1 34.3-8.6 53-2.4 96.6 41.2 94.1 94.1-.6 12.2-3.6 23.8-8.6 34.3l53.5 53.5c33-25.3 61.3-55.9 85-82z"></path>
                                <path d="M192.2 260.9c2.4 31.3 27.6 56.5 58.9 58.9 8.2.6 16.1-.3 23.4-2.6l-79.8-79.8c-2.2 7.4-3.1 15.3-2.5 23.5zM320 256c0-1.3-.1-2.6-.1-3.9-5.6 2.5-11.7 3.9-18.2 3.9-1.1 0-2.1 0-3.1-.1l18.6 18.7c1.8-5.9 2.8-12.2 2.8-18.6zM256 209c0-6 1.1-11.7 3.1-16.9-1 0-2-.1-3.1-.1-6.4 0-12.6 1-18.5 2.8l18.7 18.7c-.1-1.5-.2-3-.2-4.5z"></path>
                            </svg>
                        </button>
                    </div>
                </div>

                {/* Botão de envio habilitado */}
                <button className="btn btn-primary btn-block btn-lg" type="submit">
                    Confirmar e continuar
                </button>
            </form>
        </div>
    );
}

export default CriarConta;