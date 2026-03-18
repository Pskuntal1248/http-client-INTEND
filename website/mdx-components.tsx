import { useMDXComponents as getDocsMDXComponents } from 'nextra-theme-docs'
import { Callout, Cards, Tabs, Steps } from 'nextra/components'

const docsComponents = getDocsMDXComponents()

export function useMDXComponents(components?: Record<string, React.ComponentType>) {
  return {
    ...docsComponents,
    Callout,
    Cards,
    Card: Cards.Card,
    Tabs,
    Steps,
    ...components,
  }
}
