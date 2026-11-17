<template>
  <div class="app">
    <header>
      <div>
        <h1>SkillUp</h1>
        <p class="subtitulo">Formacao de colaboradores com ML, RAG e agentes de IA</p>
      </div>
      <nav>
        <button v-for="aba in abas" :key="aba.id" :class="{ active: atual === aba.id }" @click="atual = aba.id">
          {{ aba.nome }}
        </button>
      </nav>
    </header>

    <main>
      <ColaboradoresView v-if="atual === 'colaboradores'" />
      <ClassificacaoView v-if="atual === 'classificacao'" />
      <TreinosView v-if="atual === 'treinos'" />
      <RecomendacaoView v-if="atual === 'recomendacao'" />
      <AgentesView v-if="atual === 'agentes'" />
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { carregarColaboradores } from './api.js'
import ColaboradoresView from './components/ColaboradoresView.vue'
import ClassificacaoView from './components/ClassificacaoView.vue'
import TreinosView from './components/TreinosView.vue'
import RecomendacaoView from './components/RecomendacaoView.vue'
import AgentesView from './components/AgentesView.vue'

const abas = [
  { id: 'colaboradores', nome: 'Colaboradores e cursos' },
  { id: 'classificacao', nome: 'Classificacao (ML)' },
  { id: 'treinos', nome: 'Treinos do modelo' },
  { id: 'recomendacao', nome: 'Recomendacao (RAG)' },
  { id: 'agentes', nome: 'Agentes de IA' }
]
const atual = ref('colaboradores')

onMounted(() => carregarColaboradores().catch(() => {}))
</script>

<style>
* { box-sizing: border-box; margin: 0; padding: 0; }
body { font-family: system-ui, sans-serif; background: #f0f2f5; color: #333; }
.app { max-width: 1100px; margin: 0 auto; padding: 20px; }
header {
  background: #1565c0; color: white; padding: 16px 24px; border-radius: 8px; margin-bottom: 24px;
  display: flex; align-items: center; justify-content: space-between; gap: 16px; flex-wrap: wrap;
}
header h1 { font-size: 1.5rem; }
.subtitulo { font-size: 0.85rem; opacity: 0.85; }
nav { display: flex; gap: 8px; flex-wrap: wrap; }
nav button {
  background: transparent; color: white; border: 2px solid rgba(255,255,255,0.5); padding: 6px 12px;
  border-radius: 4px; cursor: pointer; font-size: 0.85rem;
}
nav button.active, nav button:hover { background: white; color: #1565c0; border-color: white; }
main { display: flex; flex-direction: column; gap: 20px; }
.grade { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
@media (max-width: 800px) { .grade { grid-template-columns: 1fr; } }
.card { background: white; border-radius: 8px; padding: 20px; box-shadow: 0 1px 4px rgba(0,0,0,0.1); }
.card h2 { font-size: 1.05rem; margin-bottom: 4px; color: #1565c0; }
.card .req { font-size: 0.75rem; color: #777; margin-bottom: 14px; }
.form-group { display: flex; flex-direction: column; gap: 4px; margin-bottom: 10px; }
.linha { display: flex; gap: 10px; flex-wrap: wrap; align-items: flex-end; }
.linha .form-group { flex: 1; min-width: 140px; }
label { font-size: 0.8rem; font-weight: 600; color: #555; }
input, select, textarea { padding: 8px 10px; border: 1px solid #ccc; border-radius: 4px; font-size: 0.9rem; font-family: inherit; }
textarea { min-height: 60px; resize: vertical; }
.btn { padding: 8px 16px; border: none; border-radius: 4px; cursor: pointer; font-size: 0.85rem; font-weight: 600; }
.btn:disabled { opacity: 0.5; cursor: default; }
.btn-primary { background: #1565c0; color: white; }
.btn-success { background: #2e7d32; color: white; }
.btn-danger { background: #c62828; color: white; }
.btn-light { background: #e3e8ef; color: #333; }
.btn-sm { padding: 4px 10px; font-size: 0.78rem; }
.alert { padding: 10px 14px; border-radius: 4px; font-size: 0.85rem; margin-top: 10px; }
.alert-success { background: #e8f5e9; color: #2e7d32; border: 1px solid #a5d6a7; }
.alert-error { background: #ffebee; color: #c62828; border: 1px solid #ef9a9a; }
.alert-info { background: #e3f2fd; color: #1565c0; border: 1px solid #90caf9; }
table { width: 100%; border-collapse: collapse; font-size: 0.85rem; }
th, td { text-align: left; padding: 6px 8px; border-bottom: 1px solid #eee; vertical-align: top; }
th { color: #666; font-weight: 600; }
.badge { display: inline-block; padding: 2px 8px; border-radius: 10px; font-size: 0.72rem; font-weight: 700; background: #eceff1; color: #455a64; }
.badge.INICIANTE { background: #fff3e0; color: #e65100; }
.badge.INTERMEDIARIO { background: #e3f2fd; color: #1565c0; }
.badge.AVANCADO { background: #e8f5e9; color: #2e7d32; }
.badge.PENDENTE, .badge.NA_FILA { background: #fff8e1; color: #f57f17; }
.badge.PROCESSANDO { background: #e3f2fd; color: #1565c0; }
.badge.EXECUTADA, .badge.CONCLUIDO { background: #e8f5e9; color: #2e7d32; }
.badge.REJEITADA, .badge.FALHOU, .badge.ERRO_PUBLICACAO { background: #ffebee; color: #c62828; }
.muted { color: #888; font-size: 0.8rem; }
.trecho { border-left: 3px solid #90caf9; padding: 6px 10px; margin-top: 8px; background: #fafcff; font-size: 0.83rem; }
</style>
