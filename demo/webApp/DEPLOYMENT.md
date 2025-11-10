# Web App Deployment Guide

This guide explains how to deploy the Stellar SDK web demo to production.

## Building for Production

Build the production bundle:

```bash
./gradlew :demo:webApp:productionDist
```

This creates an optimized build in `demo/webApp/dist/` containing:
- `index.html` - Main HTML file
- `app-kotlin-stdlib.js` - Kotlin stdlib (19 MB)
- `app-vendors.js` - Third-party libraries (1 MB)
- `app.js` - Application code (9 KB)
- `skiko.wasm` - Compose rendering engine (8 MB)
- `bccfa839aa4b38489c76.wasm` - Additional Compose resources (8 MB)
- `wasm/` - Smart contract WASM files directory
  - `soroban_hello_world_contract.wasm`
  - `soroban_auth_contract.wasm`
  - `soroban_token_contract.wasm`
  - `soroban_atomic_swap_contract.wasm`
- Source maps (.js.map files)

## Deployment Requirements

### 1. Upload All Files

Upload the ENTIRE `dist/` directory to your web server, including:
- All JavaScript files
- All WASM files (both root and wasm/ subdirectory)
- The index.html file
- Subdirectories (wasm/, composeResources/)

### 2. Web Server Configuration

#### MIME Types

Ensure your web server serves WASM files with the correct MIME type:

**Nginx**:
```nginx
location / {
    root /var/www/stellar-demo;

    # Serve WASM files with correct MIME type
    location ~* \.wasm$ {
        types { application/wasm wasm; }
    }

    # Enable compression for JS files
    gzip on;
    gzip_types application/javascript text/javascript;
}
```

**Apache** (`.htaccess`):
```apache
# Serve WASM files with correct MIME type
AddType application/wasm .wasm

# Enable compression for JS files
<IfModule mod_deflate.c>
    AddOutputFilterByType DEFLATE application/javascript text/javascript
</IfModule>
```

#### Subdirectory Deployments

If deploying to a subdirectory (e.g., `https://example.com/stellar-demo/`):

The application will automatically try multiple paths to find WASM files:
1. `./wasm/filename.wasm` (relative to current page)
2. `wasm/filename.wasm` (relative without leading ./)
3. `/wasm/filename.wasm` (absolute from root)

For subdirectory deployments, ensure the wasm/ directory is in the same directory as index.html.

## Common Deployment Platforms

### GitHub Pages

1. Build the production bundle:
   ```bash
   ./gradlew :demo:webApp:productionDist
   ```

2. Create a `.nojekyll` file in the dist directory:
   ```bash
   touch demo/webApp/dist/.nojekyll
   ```

3. Push to gh-pages branch:
   ```bash
   cd demo/webApp/dist
   git init
   git add .
   git commit -m "Deploy to GitHub Pages"
   git push -f git@github.com:your-username/repo.git master:gh-pages
   ```

### Netlify

1. Build command: `./gradlew :demo:webApp:productionDist`
2. Publish directory: `demo/webApp/dist`
3. No additional configuration needed (Netlify handles MIME types correctly)

### Vercel

1. Build command: `./gradlew :demo:webApp:productionDist`
2. Output directory: `demo/webApp/dist`
3. No additional configuration needed

### Firebase Hosting

1. Install Firebase CLI:
   ```bash
   npm install -g firebase-tools
   ```

2. Initialize Firebase:
   ```bash
   firebase init hosting
   ```
   - Public directory: `demo/webApp/dist`
   - Single-page app: No
   - GitHub auto-deploy: Optional

3. Deploy:
   ```bash
   ./gradlew :demo:webApp:productionDist
   firebase deploy --only hosting
   ```

### AWS S3 + CloudFront

1. Build the production bundle:
   ```bash
   ./gradlew :demo:webApp:productionDist
   ```

2. Upload to S3:
   ```bash
   aws s3 sync demo/webApp/dist s3://your-bucket-name/ --delete
   ```

3. Set WASM MIME type:
   ```bash
   aws s3 cp s3://your-bucket-name/ s3://your-bucket-name/ \
     --exclude "*" --include "*.wasm" \
     --content-type "application/wasm" \
     --metadata-directive REPLACE \
     --recursive
   ```

4. Invalidate CloudFront cache:
   ```bash
   aws cloudfront create-invalidation \
     --distribution-id YOUR_DISTRIBUTION_ID \
     --paths "/*"
   ```

## Testing Production Build Locally

Preview the production build with Vite:

```bash
./gradlew :demo:webApp:vitePreview
```

This serves the dist/ directory at http://localhost:8082

## Troubleshooting

### WASM Files Not Loading (404 Error)

**Problem**: "Failed to fetch WASM file 'soroban_token_contract.wasm': HTTP 404"

**Solutions**:

1. **Verify files were uploaded**:
   - Check that `wasm/` directory exists in your deployment
   - Confirm all .wasm files are present

2. **Check web server configuration**:
   - Ensure server serves .wasm files with `application/wasm` MIME type
   - Verify directory listing is disabled for security

3. **Test with browser DevTools**:
   - Open Network tab
   - Try to deploy a contract
   - Check the failed request URL
   - Verify it matches your server structure

4. **Check base path**:
   - If deployed to subdirectory, ensure wasm/ is relative to index.html
   - The app tries multiple paths automatically

### Large Bundle Size

The production bundle is approximately 28 MB uncompressed (20 MB JavaScript + 8 MB WASM). This is expected for a Compose Multiplatform web app. To reduce load time:

1. **Enable gzip compression** on your web server (reduces JavaScript from 20 MB to ~2.9 MB; WASM stays 8 MB)
2. **Use CDN** for faster global distribution
3. **Enable HTTP/2** for parallel file downloads
4. **Total download size**: ~11 MB (2.9 MB JS gzipped + 8 MB WASM uncompressed)

**Note**: WASM files cannot be compressed with gzip, so the Skiko rendering engine (8 MB) always transfers at full size.

### Memory Issues During Build

If you encounter memory errors during production build:

```bash
export NODE_OPTIONS="--max-old-space-size=4096"
./gradlew :demo:webApp:productionDist
```

## Security Considerations

1. **CORS**: The app makes requests to Stellar Testnet. Ensure CORS is properly configured if adding custom APIs.

2. **Content Security Policy**: If using CSP headers, allow `unsafe-eval` for Kotlin/JS and `wasm-unsafe-eval` for WebAssembly.

3. **HTTPS**: Always deploy production builds over HTTPS (required for secure crypto operations).

## Bundle Size Analysis

Production bundle breakdown:

**JavaScript Files** (compressible):
- Kotlin stdlib: 19 MB → 2.5 MB gzipped (Kotlin language runtime)
- Vendors: 1 MB → 325 KB gzipped (libsodium, Compose libraries)
- App code: 8.5 KB → 2.4 KB gzipped (demo application logic)
- **JS Total**: 20 MB → 2.9 MB gzipped

**WebAssembly Files** (not compressible):
- Skiko WASM: 8 MB (Compose rendering engine)
- Contract WASMs: ~11 KB total (Stellar smart contracts)
- **WASM Total**: 8 MB (always uncompressed)

**Total Bundle**:
- Uncompressed: 28 MB (20 MB JS + 8 MB WASM)
- Download size: ~11 MB (2.9 MB JS gzipped + 8 MB WASM)

## Support

For deployment issues:
1. Check this guide first
2. Verify your build succeeded: `./gradlew :demo:webApp:productionDist`
3. Test locally: `./gradlew :demo:webApp:vitePreview`
4. Check browser console for detailed error messages
