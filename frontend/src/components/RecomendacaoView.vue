<template>
  <div class="card">
    <h2>Recomendar cursos com RAG</h2>
    <p class="req">RF03 · ai-service (Spring AI) · recupera trechos dos documentos institucionais e gera a recomendacao</p>
    <div class="linha">
      <div class="form-group">
        <label>Colaborador</label>
        <select v-model="colaboradorId">
          <option v-for="c in colaboradores" :key="c.id" :value="c.id">{{ c.nome }} ({{ c.nivel }})</option>
        </select>
      </div>
      <div class="form-group" style="flex: 2">
        <label>Interesses</label>
        <input v-model="interesses" placeholder="ex.: quero aprender docker e APIs" />
      </div>
      <button class="btn btn-primary" :disabled="!colaboradorId || carregando" @click="recomendar">
        {{ carregando ? 'Pensando...' : 'Recomendar' }}
      </button>
    </div>
    <div v-if="erro" class="alert alert-error">{{ erro }}</div>
  </div>

  <div class="grade" v-if="resultado">
    <div class="card">
      <h2>Cursos recomendados</h2>
      <p class="req">Modo da IA: <span class="badge">{{ resultado.modo }}</span> ·
        nivel usado: <span :class="['badge', resultado.perfil.nivel]">{{ resultado.perfil.nivel }}</span></p>
      <div v-for="r in resultado.recomendacoes" :key="r.cursoId" class="trecho">
        <strong>{{ r.nome }}</strong>
        <p>{{ r.motivo }}</p>
      </div>
      <p v-if="!resultado.recomendacoes.length" class="muted">Nenhum curso encontrado</p>
    </div>
    <div class="card">
      <h2>Trechos recuperados (contexto do RAG)</h2>
      <p class="req">Consulta: {{ resultado.consulta }}</p>
      <div v-for="(t, i) in resultado.trechos" :key="i" class="trecho">
        <strong>{{ t.titulo }}</strong> <span class="muted">({{ t.fonte }})</span>
        <p>{{ t.texto }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { api, colaboradores, carregarColaboradores } from '../api.js'

const colaboradorId = ref(null)
const interesses = ref('')
const resultado = ref(null)
const erro = ref('')
const carregando = ref(false)

async function recomendar() {
  erro.value = ''; carregando.value = true
  try {
    resultado.value = await api('/ai/recomendacoes', {
      method: 'POST', body: { colaboradorId: colaboradorId.value, interesses: interesses.value }
    })
  } catch (e) {
    erro.value = e.message
  } finally {
    carregando.value = false
  }
}

onMounted(async () => {
  await carregarColaboradores()
  if (colaboradores.value.length) colaboradorId.value = colaboradores.value[0].id
})
</script>
