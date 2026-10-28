import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { api } from '../../services/api.js';
import './FormOportunidade.css';

const FORM_VAZIO = {
  titulo: '',
  categoriaId: '',
  descricao: '',
  data: '',
  horario: '',
  cidade: '',
  estado: '',
  vagas: 1,
  status: 'ABERTA',
};

// Mesmo formulário para CRIAR (/painel/oportunidades/nova) e EDITAR (/painel/oportunidades/:id/editar)
function FormOportunidade() {
  const { id } = useParams();
  const editando = Boolean(id);
  const navigate = useNavigate();

  const [form, setForm] = useState(FORM_VAZIO);
  const [categorias, setCategorias] = useState([]);
  const [ongId, setOngId] = useState(null);
  const [carregando, setCarregando] = useState(true);
  const [salvando, setSalvando] = useState(false);
  const [erro, setErro] = useState('');

  useEffect(() => {
    async function carregar() {
      try {
        const [listaCategorias, minhaOng] = await Promise.all([
          api('/categorias'),
          api('/ongs/me'),
        ]);
        setCategorias(listaCategorias);
        setOngId(minhaOng.id);

        if (editando) {
          const op = await api(`/oportunidades/${id}`);
          if (op.ongId !== minhaOng.id) {
            setErro('Você só pode editar oportunidades da sua própria ONG.');
          } else {
            setForm({
              titulo: op.titulo ?? '',
              categoriaId: String(op.categoriaId),
              descricao: op.descricao ?? '',
              data: op.data ?? '',
              horario: (op.horario ?? '').slice(0, 5),
              cidade: op.cidade ?? '',
              estado: op.estado ?? '',
              vagas: op.vagas,
              status: op.status ?? 'ABERTA',
            });
          }
        }
      } catch (e) {
        setErro(e.message);
      } finally {
        setCarregando(false);
      }
    }
    carregar();
  }, [id, editando]);

  function alterar(e) {
    const { name, value } = e.target;
    setForm((atual) => ({
      ...atual,
      [name]: name === 'estado' ? value.toUpperCase() : value,
    }));
  }

  async function enviar(e) {
    e.preventDefault();
    setErro('');

    if (form.estado && form.estado.length !== 2) {
      setErro('O estado deve ter 2 letras (ex: RS).');
      return;
    }

    const payload = {
      ongId,
      categoriaId: Number(form.categoriaId),
      titulo: form.titulo.trim(),
      descricao: form.descricao.trim(),
      data: form.data,
      horario: form.horario,
      cidade: form.cidade.trim(),
      estado: form.estado || null,
      vagas: Number(form.vagas),
      status: form.status,
    };

    setSalvando(true);
    try {
      if (editando) {
        await api(`/oportunidades/${id}`, { method: 'PUT', body: payload });
      } else {
        await api('/oportunidades', { method: 'POST', body: payload });
      }
      navigate('/painel/oportunidades');
    } catch (e2) {
      setErro(e2.message);
    } finally {
      setSalvando(false);
    }
  }

  if (carregando) return <p className="form-op-aviso">Carregando...</p>;

  return (
    <main className="form-op">
      <h1>{editando ? 'Editar oportunidade' : 'Criar oportunidade'}</h1>

      {erro && <p className="form-op-erro" role="alert">{erro}</p>}

      <form onSubmit={enviar}>
        <label>
          Título
          <input name="titulo" value={form.titulo} onChange={alterar} maxLength={200} required />
        </label>

        <label>
          Categoria (tipo de atividade)
          <select name="categoriaId" value={form.categoriaId} onChange={alterar} required>
            <option value="">Selecione...</option>
            {categorias.map((c) => (
              <option key={c.id} value={c.id}>{c.nome}</option>
            ))}
          </select>
        </label>

        <label>
          Descrição
          <textarea name="descricao" value={form.descricao} onChange={alterar} rows={4} />
        </label>

        <div className="form-op-linha">
          <label>
            Data
            <input type="date" name="data" value={form.data} onChange={alterar} required />
          </label>
          <label>
            Horário
            <input type="time" name="horario" value={form.horario} onChange={alterar} required />
          </label>
        </div>

        <div className="form-op-linha">
          <label className="form-op-grande">
            Cidade
            <input name="cidade" value={form.cidade} onChange={alterar} maxLength={100} />
          </label>
          <label>
            Estado
            <input name="estado" value={form.estado} onChange={alterar} maxLength={2} placeholder="RS" />
          </label>
        </div>

        <div className="form-op-linha">
          <label>
            Vagas
            <input type="number" name="vagas" value={form.vagas} onChange={alterar} min={1} required />
          </label>

          {editando && (
            <label>
              Status
              <select name="status" value={form.status} onChange={alterar}>
                <option value="ABERTA">Aberta</option>
                <option value="ENCERRADA">Encerrada</option>
                <option value="CANCELADA">Cancelada</option>
              </select>
            </label>
          )}
        </div>

        <div className="form-op-acoes">
          <Link to="/painel/oportunidades" className="form-op-cancelar">Cancelar</Link>
          <button type="submit" disabled={salvando || !ongId}>
            {salvando ? 'Salvando...' : editando ? 'Salvar alterações' : 'Publicar oportunidade'}
          </button>
        </div>
      </form>
    </main>
  );
}

export default FormOportunidade;
