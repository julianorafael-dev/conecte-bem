import { useState } from 'react';
import { IoEye, IoEyeOff } from 'react-icons/io5';
import './CriarConta.css';

function CriarConta() {
    // 1. Estados do Formulário
    const [formData, setFormData] = useState({
        email: '',
        tipoPessoa: 'CPF',
        nomeCompleto: '',
        dataNascimento: '',
        cpfCnpj: '',
        telefone: '',
        senha: '',
        confirmarSenha: ''
    });

    // Data de hoje, usada como limite do campo de data
    const dataHoje = new Date().toLocaleDateString('en-CA');

    const senhasDiferentes = formData.confirmarSenha !== '' && formData.senha !== formData.confirmarSenha;

    // Estado para controlar a visibilidade da senha
    const [mostrarSenha, setMostrarSenha] = useState(false);

    const [mostrarConfirmarSenha, setMostrarConfirmarSenha] = useState(false);

    // 2. Função para alternar a exibição da senha
    const toggleMostrarSenha = () => {
        setMostrarSenha(!mostrarSenha);
    };

    const toggleMostrarConfirmarSenha = () => {
        setMostrarConfirmarSenha(!mostrarConfirmarSenha);
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

        if (formData.senha !== formData.confirmarSenha) {
            return;
        }

        // Exemplo de ação ao enviar o formulário
        alert('Cadastro realizado com sucesso! Por favor, clique em "Entrar" no menu superior');
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
                    <label htmlFor="nome-completo">{formData.tipoPessoa === 'CPF' ? 'Nome Completo' : 'Razão Social'}</label>
                    <input 
                        id="nome-completo" 
                        name="nomeCompleto" 
                        type="text" 
                        className="form-control" 
                        required 
                        placeholder="Nome Completo ou Razão Social" 
                        minLength="4" 
                        value={formData.nomeCompleto}
                        onChange={handleChange}
                    />
                </div>

                {/* Campo de entrada para a Data de Nascimento - Data de criação da ONG */}                            
              
                <div className="form-grupo-nascimento">
                    <label htmlFor="data-de-nascimento">{formData.tipoPessoa === 'CPF' ? 'Insira sua data de nascimento' : 'Insira a data de criação da ONG'}</label>
                    <input 
                        id="data-de-nascimento" 
                        name="dataNascimento" 
                        type="date" 
                        className="form-control" 
                        required
                        max={dataHoje}
                        value={formData.dataNascimento}
                        onChange={handleChange}
                    />
                </div>             
                

                <div className="nascimento-texto">{formData.tipoPessoa === 'CPF' ? 'É preciso ter 18 anos ou mais para criar uma conta.' : '.'}</div>

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
                            {mostrarSenha ? <IoEyeOff size={20} /> : <IoEye size={20} />}
                        </button>
                    </div>
                </div>

                {/* Repetir senha */}
                <div className="form-grupo">
                    <label htmlFor="confirmar-senha">Repetir senha</label>
                    <div className="form-control-password form-control">
                        <input
                            id="confirmar-senha"
                            name="confirmarSenha"
                            type={mostrarConfirmarSenha ? "text" : "password"}
                            className="form-control form-control-inner-password"
                            required
                            minLength="6"
                            placeholder="Digite a senha novamente"
                            value={formData.confirmarSenha}
                            onChange={handleChange}
                        />
                        <button
                            type="button"
                            className="toggle-show-password"
                            onClick={toggleMostrarConfirmarSenha}
                            title={mostrarConfirmarSenha ? "Ocultar senha" : "Mostrar senha"}
                        >
                            {mostrarConfirmarSenha ? <IoEyeOff size={20} /> : <IoEye size={20} />}
                        </button>
                    </div>
                    {senhasDiferentes && (
                        <span className="form-erro">As senhas não coincidem.</span>
                    )}
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