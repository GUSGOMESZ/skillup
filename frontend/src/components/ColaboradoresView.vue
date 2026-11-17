<template>
  <div class="grade">
    <div class="card">
      <h2>Cadastrar colaborador</h2>
      <p class="req">RF01 · core-service (monolito modular, hexagonal)</p>
      <form @submit.prevent="cadastrarColaborador">
        <div class="form-group"><label>Nome</label><input v-model="novo.nome" required /></div>
        <div class="form-group"><label>E-mail</label><input v-model="novo.email" type="email" required /></div>
        <div class="linha">
          <div class="form-group"><label>Cargo</label><input v-model="novo.cargo" /></div>
          <div class="form-group"><label>Area</label><input v-model="novo.area" /></div>
        </div>
        <button class="btn btn-primary">Cadastrar</button>
      </form>
      <div v-if="msgColab" :class="['alert', erroColab ? 'alert-error' : 'alert-success']">{{ msgColab }}</div>
    </div>

    <div class="card">
      <h2>Cadastrar curso</h2>
      <p class="req">RF01 · catalogo usado tambem pela recomendacao</p>
      <form @submit.prevent="cadastrarCurso">
        <div class="linha">
          <div class="form-group"><label>Nome</label><input v-model="curso.nome" required /></div>
          <div class="form-group"><label>Tipo</label><input v-model="curso.tipo" placeholder="PROGRAMACAO" required /></div>
        </div>
        <div class="form-group"><label>Descricao</label><textarea v-model="curso.descricao" /></div>
        <button class="btn btn-primary">Cadastrar curso</button>
      </form>
      <div v-if="msgCurso" :class="['alert', erroCurso ? 'alert-error' : 'alert-success']">{{ msgCurso }}</div>
    </div>
  </div>

  <div class="card">
    <h2>Colaboradores</h2>
    <p class="req">E-mail mascarado na listagem (RNF03 · protecao de dados pessoais)</p>
    <table>
      <thead><tr><th>ID</th><th>Nome</th><th>E-mail</th><th>Cargo</th><th>Area</th><th>Nivel</th><th></th></tr></thead>
      <tbody>
        <tr v-for="c in colaboradores" :key="c.id">
          <td>{{ c.id }}</td><td>{{ c.nome }}</td><td>{{ c.email }}</td><td>{{ c.cargo }}</td><td>{{ c.area }}</td>
          <td><span :class="['badge', c.nivel]">{{ c.nivel }}</span></td>
          <td><button class="btn btn-light btn-sm" @click="abrir(c)">Detalhes</button></td>
        </tr>
      </tbody>
    </table>
  </div>

  <div class="card" v-if="selecionado">
    <h2>{{ selecionado.nome }}: matriculas e acompanhamentos</h2>
    <p class="req">Origem AGENTE = criado por um agente de IA depois da aprovacao do usuario (RF04)</p>
    <div class="linha">
      <div class="form-group">
        <label>Matricular manualmente em</label>
        <select v-model="cursoMatricula">
          <option v-for="c in cursos" :key="c.id" :value="c.id">{{ c.nome }}</option>
        </select>
      </div>
      <button class="btn btn-success" :disabled="!cursoMatricula" @click="matricular">Matricular</button>
    </div>
    <div v-if="msgMatricula" :class="['alert', erroMatricula ? 'alert-error' : 'alert-success']">{{ msgMatricula }}</div>
    <div class="grade" style="margin-top: 14px">
      <table>
        <thead><tr><th>Curso</th><th>Origem</th><th>Data</th></tr></thead>
        <tbody>
          <tr v-for="m in matriculas" :key="m.id"><td>{{ m.cursoNome }}</td><td>{{ m.origem }}</td><td>{{ m.data }}</td></tr>
          <tr v-if="!matriculas.length"><td colspan="3" class="muted">Nenhuma matricula</td></tr>
        </tbody>
      </table>
      <table>
        <thead><tr><th>Acompanhamento</th><th>Origem</th><th>Data</th></tr></thead>
        <tbody>
          <tr v-for="a in acompanhamentos" :key="a.id"><td>{{ a.descricao }}</td><td>{{ a.origem }}</td><td>{{ a.dataPrevista }}</td></tr>
          <tr v-if="!acompanhamentos.length"><td colspan="3" class="muted">Nenhum acompanhamento</td></tr>
        </tbody>
      </table>
    </div>
  </div>

  <div class="card">
    <h2>Catalogo de cursos</h2>
    <table>
      <thead><tr><th>ID</th><th>Nome</th><th>Tipo</th><th>Descricao</th></tr></thead>
      <tbody>
        <tr v-for="c in cursos" :key="c.id"><td>{{ c.id }}</td><td>{{ c.nome }}</td><td>{{ c.tipo }}</td><td>{{ c.descricao }}</td></tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { api, colaboradores, carregarColaboradores } from '../api.js'

const novo = ref({ nome: '', email: '', cargo: '', area: '' })
const curso = ref({ nome: '', tipo: '', descricao: '' })
const cursos = ref([])
const selecionado = ref(null)
const matriculas = ref([])
const acompanhamentos = ref([])
const cursoMatricula = ref(null)
const msgColab = ref(''); const erroColab = ref(false)
const msgCurso = ref(''); const erroCurso = ref(false)
const msgMatricula = ref(''); const erroMatricula = ref(false)

async function carregarCursos() {
  cursos.value = await api('/core/cursos')
}

async function cadastrarColaborador() {
  try {
    const c = await api('/core/colaboradores', { method: 'POST', body: novo.value })
    msgColab.value = `Colaborador ${c.nome} cadastrado (id ${c.id})`; erroColab.value = false
    novo.value = { nome: '', email: '', cargo: '', area: '' }
    await carregarColaboradores()
  } catch (e) { msgColab.value = e.message; erroColab.value = true }
}

async function cadastrarCurso() {
  try {
    const c = await api('/core/cursos', { method: 'POST', body: curso.value })
    msgCurso.value = `Curso ${c.nome} cadastrado (id ${c.id})`; erroCurso.value = false
    curso.value = { nome: '', tipo: '', descricao: '' }
    await carregarCursos()
  } catch (e) { msgCurso.value = e.message; erroCurso.value = true }
}

async function abrir(c) {
  selecionado.value = c
  msgMatricula.value = ''
  matriculas.value = await api(`/core/colaboradores/${c.id}/matriculas`)
  acompanhamentos.value = await api(`/core/colaboradores/${c.id}/acompanhamentos`)
}

async function matricular() {
  try {
    await api(`/core/colaboradores/${selecionado.value.id}/matriculas`, {
      method: 'POST', body: { cursoId: cursoMatricula.value, origem: 'MANUAL' }
    })
    msgMatricula.value = 'Matricula criada'; erroMatricula.value = false
    await abrir(selecionado.value)
  } catch (e) { msgMatricula.value = e.message; erroMatricula.value = true }
}

onMounted(async () => {
  await Promise.all([carregarColaboradores(), carregarCursos()])
})
</script>
