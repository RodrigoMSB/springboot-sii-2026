-- Convierte los divs `::: vasbien` del Markdown en entornos LaTeX del preámbulo.
-- Sin esto pandoc ignora la clase del div y el recuadro no sale.
local entornos = { vasbien = true, atasco = true, metafora = true, nota = true }

function Div(el)
  for _, clase in ipairs(el.classes) do
    if entornos[clase] then
      return {
        pandoc.RawBlock('latex', '\\begin{' .. clase .. '}'),
        el,
        pandoc.RawBlock('latex', '\\end{' .. clase .. '}')
      }
    end
  end
end
