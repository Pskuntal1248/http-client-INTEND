import nextra from 'nextra'

const withNextra = nextra({
  contentDirBasePath: '/docs'
})

export default withNextra({
  async rewrites() {
    return [
      {
        source: '/_pagefind/:path*',
        destination: '/_next/static/chunks/_pagefind/:path*',
      },
    ]
  },
})
