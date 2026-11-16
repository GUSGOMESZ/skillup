<template>
  <div class="card">
    <h2>Classificar nivel de competencia</h2>
    <p class="req">RF02 · ml-service (FastAPI + Regressao Logistica) · chamada REST sincrona, meta de ate 2 s (RNF01)</p>

    <div class="form-group">
      <label>Colaborador</label>
      <select v-model="colaboradorId">
        <option v-for="c in colaboradores" :key="c.id" :value="c.id">{{ c.nome }} ({{ c.nivel }})</option>
      </select>
    </div>

    <p class="muted" style="margin-bottom: 8px">Exemplos prontos:
      <button class="btn btn-light btn-sm" @click="preencher(6, 2, 6, 4)">Iniciante</button>
      <button class="btn btn-light btn-sm" @click="preencher(48, 12, 8, 10)">Intermediario</button>
      <button class="btn btn-light btn-sm" @click="preencher(96, 25, 9, 15)">Avancado</button>
    </p>
    <div class="linha">
      <div class="form-group"><label>Meses de experiencia</label><input type="number" v-model.number="perfil.meses_experiencia" min="0" /></div>
      <div class="form-group"><label>Cursos concluidos</label><input type="number" v-model.number="perfil.cursos_concluidos" min="0" /></div>
      <div class="form-group"><label>Media das avaliacoes (0-10)</label><input type="number" step="0.1" v-model.number="perfil.media_avaliacoes" min="0" max="10" /></div>
      <div class="form-group"><label>Horas de estudo/semana</label><input type="number" v-model.number="perfil.horas_estudo_semana" min="0" /></div>
    </div>
    <button class="btn btn-primary" :disabled="!colaboradorId || carregando" @click="classificar">
      {{ carregando ? 'Classificando...' : 'Classificar e salvar no cadastro' }}
    </button>

    <div v-if="erro" class="alert alert-error">{{ erro }}</div>
    <div v-if="resultado" class="alert alert-success">
      <p>Nivel previsto: <span :class="['badge', resultado.nivel]">{{ resultado.nivel }}</span>
        · salvo no cadastro de {{ salvoPara }}</p>
      <p class="muted" style="margin-top: 6px">
        Probabilidades:
        <span v-for="(p, nivel) in resultado.probabilidades" :key="nivel">{{ nivel }} {{ (p * 100).toFixed(0) }}% · </span>
      </p>
      <p class="muted">Modelo versao {{ resultado.modelo.versao }} · acuracia {{ (resultado.modelo.acuracia * 100).toFixed(1) }}% ·
        inferencia em {{ resultado.tempo_ms }} ms · ida e volta pelo gateway em {{ tempoTotal }} ms</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { api, colaboradores, carregarColaboradores } from '../api.js'

const colaboradorId = ref(null)
const perfil = ref({ meses_experiencia: 6, cursos_concluidos: 2, media_avaliacoes: 6, horas_estudo_semana: 4 })
const resultado = ref(null)
const salvoPara = ref('')
const tempoTotal = ref(0)
const erro = ref('')
const carregando = ref(false)

function preencher(meses, cursos, media, horas) {
  perfil.value = { meses_experiencia: meses, cursos_concluidos: cursos, media_avaliacoes: media, horas_estudo_semana: horas }
}

async function classificar() {
  erro.value = ''; resultado.value = null; carregando.value = true
  try {
    const inicio = performance.now()
    const r = await api('/ml/predict', { method: 'POST', body: perfil.value })
    tempoTotal.value = Math.round(performance.now() - inicio)
    const c = await api(`/core/colaboradores/${colaboradorId.value}/nivel`, { method: 'PUT', body: { nivel: r.nivel } })
    resultado.value = r
    salvoPara.value = c.nome
    await carregarColaboradores()
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
