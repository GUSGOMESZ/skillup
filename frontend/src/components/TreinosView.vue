<template>
  <div class="grade">
    <div class="card">
      <h2>Solicitar treino do modelo</h2>
      <p class="req">RF05 · comunicacao assincrona: ml-service publica no RabbitMQ e responde 202 na hora</p>
      <div class="linha">
        <div class="form-group"><label>Seed (semente do dataset)</label><input type="number" v-model.number="seed" /></div>
        <button class="btn btn-primary" @click="solicitar">Solicitar treino</button>
      </div>
      <div v-if="mensagem" :class="['alert', erro ? 'alert-error' : 'alert-info']">{{ mensagem }}</div>
    </div>

    <div class="card">
      <h2>Fila ml.training.requests</h2>
      <p class="req">RNF02 · com o worker parado os pedidos continuam sendo aceitos e ficam guardados aqui</p>
      <p v-if="fila">Mensagens esperando: <strong>{{ fila.mensagens_esperando }}</strong></p>
      <p v-if="fila">Workers conectados: <strong>{{ fila.workers_conectados }}</strong>
        <span v-if="fila.workers_conectados === 0" class="badge FALHOU">worker parado</span></p>
      <p v-if="erroFila" class="muted">{{ erroFila }}</p>
      <p class="muted" style="margin-top: 10px">Painel do RabbitMQ: <a href="http://localhost:15672" target="_blank">localhost:15672</a> (guest / guest)</p>
    </div>
  </div>

  <div class="card">
    <h2>Acompanhamento dos treinos</h2>
    <p class="req">Atualiza sozinho a cada 2 segundos (NA_FILA → PROCESSANDO → CONCLUIDO)</p>
    <table>
      <thead><tr><th>Job</th><th>Status</th><th>Seed</th><th>Criado em</th><th>Acuracia</th><th>F1</th><th>Observacao</th></tr></thead>
      <tbody>
        <tr v-for="j in jobs" :key="j.job_id">
          <td>{{ j.job_id }}</td>
          <td><span :class="['badge', j.status]">{{ j.status }}</span></td>
          <td>{{ j.seed }}</td>
          <td>{{ j.criado_em }}</td>
          <td>{{ j.metricas ? (j.metricas.acuracia * 100).toFixed(1) + '%' : '-' }}</td>
          <td>{{ j.metricas ? j.metricas.f1_macro : '-' }}</td>
          <td class="muted">{{ j.erro || '' }}</td>
        </tr>
        <tr v-if="!jobs.length"><td colspan="7" class="muted">Nenhum treino solicitado ainda</td></tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { api } from '../api.js'

const seed = ref(42)
const jobs = ref([])
const fila = ref(null)
const erroFila = ref('')
const mensagem = ref('')
const erro = ref(false)
let timer = null

async function atualizar() {
  try { jobs.value = await api('/ml/training/jobs') } catch { /* ignora durante a subida */ }
  try { fila.value = await api('/ml/fila'); erroFila.value = '' } catch (e) { erroFila.value = e.message }
}

async function solicitar() {
  try {
    const job = await api('/ml/training/jobs', { method: 'POST', body: { seed: seed.value } })
    mensagem.value = `Pedido ${job.job_id} aceito (HTTP 202) e publicado na fila`; erro.value = false
  } catch (e) {
    mensagem.value = e.message; erro.value = true
  }
  await atualizar()
}

onMounted(() => { atualizar(); timer = setInterval(atualizar, 2000) })
onUnmounted(() => clearInterval(timer))
</script>
