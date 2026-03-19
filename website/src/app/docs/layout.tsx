import { Footer, Layout, Navbar } from 'nextra-theme-docs'
import { getPageMap } from 'nextra/page-map'
import 'nextra-theme-docs/style.css'

export const metadata = {
  title: 'Intend Docs'
}

export default async function DocsLayout({ children }: { children: React.ReactNode }) {
  const pageMap = await getPageMap('/docs')
  return (
    <Layout
      pageMap={pageMap}
      docsRepositoryBase="https://github.com/pskuntal1248/http-client-intend/tree/main/website"
      navbar={<Navbar logo={<span style={{ fontWeight: 700 }}>Intend Docs</span>} />}
      footer={<Footer />}
      nextThemes={{ defaultTheme: 'dark' }}
    >
      <div className="intend-docs">{children}</div>
    </Layout>
  )
}
