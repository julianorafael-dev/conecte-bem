import './ComoFunciona.css'

function ComoFunciona() {
  return (
    <div style={{ padding: '2rem', textAlign: 'center' }}>
      <h1>Como Funciona</h1>

      <p>Entenda como conectamos voluntários e ONGs de forma simples.
        Organizações Não Governamentais (ONGs) consiste na doação espontânea de tempo, trabalho e habilidades para apoiar causas sociais, sem recebimento de remuneração.
      </p>
      
      <div className="imagem_destaque">
      <img src="https://www.itau.com.br/media/dam/m/46537b3f8b5bd535/original/voluntariado_empresarial_01.jpg" alt="Homem doando um pacote de alimento não-perecível para outro homem." />
      </div>

    </div>
  );
}

export default ComoFunciona;