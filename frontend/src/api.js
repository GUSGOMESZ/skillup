import { ref } from 'vue'

// Toda chamada passa pelo gateway: /api/core, /api/ai, /api/ml
export async function api(caminho, { method = 'GET', body } = {}) {
  const resposta = await fetch('/api' + caminho, {
    method,
    headers: body ? { 'Content-Type': 'application/json' } : undefined,
    body: body ? JSON.stringify(body) : undefined
  })
  const texto = await resposta.text()
  let dados = null
  try {
    dados = texto ? JSON.parse(texto) : null
  } catch {
    dados = texto
  }
  if (!resposta.ok) {
    let mensagem = `Erro HTTP ${resposta.status}`
    if (dados && typeof dados === 'object') {
      if (typeof dados.erro === 'string') mensagem = dados.erro
      else if (typeof dados.detail === 'string') mensagem = dados.detail
      else if (Array.isArray(dados.detail)) mensagem = 'Dados invalidos: ' + dados.detail.map(d => d.loc?.at(-1)).join(', ')
      else if (dados.message) mensagem = dados.message
    }
    throw new Error(mensagem)
  }
  return dados
}

// Lista de colaboradores compartilhada entre as abas
export const colaboradores = ref([])

export async function carregarColaboradores() {
  colaboradores.value = await api('/core/colaboradores')
  return colaboradores.value
}
