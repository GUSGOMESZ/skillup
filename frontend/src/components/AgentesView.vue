<template>
  <div class="card">
    <h2>Agentes de IA com autorizacao do usuario</h2>
    <p class="req">RF04 · o agente so propoe; nada e executado sem o seu "Aprovar" · RNF03 · cada agente so usa as ferramentas dele</p>
    <div class="linha">
      <div class="form-group">
        <label>Colaborador</label>
        <select v-model="colaboradorId" @change="atualizar">
          <option v-for="c in colaboradores" :key="c.id" :value="c.id">{{ c.nome }} ({{ c.nivel }})</option>
        </select>
      </div>
      <div class="form-group" style="flex: 2">
        <label>Interesses / assunto</label>
        <input v-model="interesses" placeholder="ex.: docker e microsservicos" />
      </div>
    </div>
    <div class="linha">
      <button class="btn btn-primary" :disabled="carregando" @click="planejar('ORIENTADOR')">Agente Orientador: propor matricula</button>
      <button class="btn btn-primary" :disabled="carregando" @click="planejar('ACOMPANHAMENTO')">Agente Acompanhamento: propor conversa</button>
      <button class="btn btn-danger" @click="testarPermissao">Testar permissao: Orientador tenta agendar</button>
    </div>
    <div v-if="mensagem" :class="['alert', erro ? 'alert-error' : 'alert-info']">{{ mensagem }}</div>
  </div>

  <div class="card">
    <h2>Acoes propostas</h2>
    <table>
      <thead><tr><th>#</th><th>Agente</th><th>Ferramenta</th><th>Proposta</th><th>Status</th><th>Resultado</th><th></th></tr></thead>
      <tbody>
        <tr v-for="a in acoes" :key="a.id">
          <td>{{ a.id }}</td>
          <td>{{ a.agente }}</td>
          <td>{{ a.ferramenta }}</td>
          <td>{{ a.descricao }}</td>
          <td><span :class="['badge', a.status]">{{ a.status }}</span></td>
          <td class="muted">{{ a.resultado || '' }}</td>
          <td style="white-space: nowrap">
            <template v-if="a.status === 'PENDENTE'">
              <button class="btn btn-success btn-sm" @click="decidir(a.id, 'aprovar')">Aprovar</button>
              <button class="btn btn-light btn-sm" @click="decidir(a.id, 'rejeitar')">Rejeitar</button>
            </template>
          </td>
        </tr>
        <tr v-if="!acoes.length"><td colspan="7" class="muted">Nenhuma acao proposta para este colaborador</td></tr>
      </tbody>
    </table>
  </div>

  <div class="grade">
    <div class="card">
      <h2>Matriculas no core</h2>
      <table>
        <tbody>
          <tr v-for="m in matriculas" :key="m.id"><td>{{ m.cursoNome }}</td><td><span class="badge">{{ m.origem }}</span></td><td>{{ m.data }}</td></tr>
          <tr v-if="!matriculas.length"><td class="muted">Nenhuma</td></tr>
        </tbody>
      </table>
    </div>
    <div class="card">
      <h2>Acompanhamentos no core</h2>
      <table>
        <tbody>
          <tr v-for="x in acompanhamentos" :key="x.id"><td>{{ x.descricao }}</td><td><span class="badge">{{ x.origem }}</span></td><td>{{ x.dataPrevista }}</td></tr>
          <tr v-if="!acompanhamentos.length"><td class="muted">Nenhum</td></tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { api, colaboradores, carregarColaboradores } from '../api.js'

const colaboradorId = ref(null)
const interesses = ref('')
const acoes = ref([])
const matriculas = ref([])
const acompanhamentos = ref([])
const mensagem = ref('')
const erro = ref(false)
const carregando = ref(false)

async function atualizar() {
  if (!colaboradorId.value) return
  const id = colaboradorId.value
  acoes.value = await api(`/ai/agentes/acoes?colaboradorId=${id}`)
  matriculas.value = await api(`/core/colaboradores/${id}/matriculas`)
  acompanhamentos.value = await api(`/core/colaboradores/${id}/acompanhamentos`)
}

async function planejar(agente) {
  carregando.value = true; mensagem.value = ''
  try {
    const propostas = await api(`/ai/agentes/${agente}/planejar`, {
      method: 'POST', body: { colaboradorId: colaboradorId.value, interesses: interesses.value }
    })
    mensagem.value = propostas.length
      ? `${agente} propos ${propostas.length} acao(oes). Aguardando sua aprovacao.`
      : `${agente} nao encontrou nada para propor.`
    erro.value = false
    await atualizar()
  } catch (e) {
    mensagem.value = e.message; erro.value = true
  } finally {
    carregando.value = false
  }
}

async function decidir(id, decisao) {
  try {
    const acao = await api(`/ai/agentes/acoes/${id}/${decisao}`, { method: 'POST' })
    mensagem.value = `Acao ${id}: ${acao.status}${acao.resultado ? ' · ' + acao.resultado : ''}`
    erro.value = acao.status === 'FALHOU'
    await atualizar()
  } catch (e) {
    mensagem.value = e.message; erro.value = true
  }
}

async function testarPermissao() {
  try {
    await api(`/ai/agentes/ORIENTADOR/ferramentas/AGENDAR_ACOMPANHAMENTO?colaboradorId=${colaboradorId.value}`, { method: 'POST' })
    mensagem.value = 'Inesperado: a acao foi aceita'; erro.value = true
  } catch (e) {
    mensagem.value = 'Bloqueado (HTTP 403): ' + e.message; erro.value = true
  }
}

onMounted(async () => {
  await carregarColaboradores()
  if (colaboradores.value.length) colaboradorId.value = colaboradores.value[0].id
  await atualizar()
})
</script>
